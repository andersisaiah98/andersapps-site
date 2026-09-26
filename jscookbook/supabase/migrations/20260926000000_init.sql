-- J's Cook Book: initial schema, Row Level Security, storage bucket and Realtime.
--
-- Run this once in the Supabase dashboard (SQL Editor › New query › paste › Run), or with the
-- Supabase CLI (`supabase db push`). It is safe to re-run: every statement is idempotent.
--
-- Conventions (they mirror the Room tables on the phone):
--   * UUID primary keys, generated on the phone.
--   * created_at / updated_at / deleted_at are epoch milliseconds written by the phone.
--     deleted_at is a soft delete; rows are never hard-deleted by the app.
--   * Every table has cookbook_id, which is what RLS checks.
--   * server_updated_at is set by a trigger on every accepted write. Phones pull "everything with
--     server_updated_at newer than what I've seen" (delta sync).
--   * Last write wins: an update whose updated_at is older than the stored row's is ignored.

-- ---------------------------------------------------------------------------------------------
-- Tables
-- ---------------------------------------------------------------------------------------------

create table if not exists public.cookbooks (
    id                uuid primary key,
    cookbook_id       uuid generated always as (id) stored,
    name              text   not null,
    created_at        bigint not null,
    updated_at        bigint not null,
    deleted_at        bigint,
    server_updated_at bigint not null default 0
);

create table if not exists public.cookbook_members (
    id                uuid primary key default gen_random_uuid(),
    cookbook_id       uuid   not null references public.cookbooks (id) on delete cascade,
    user_id           uuid   not null references auth.users (id) on delete cascade,
    role              text   not null default 'member' check (role in ('owner', 'member')),
    created_at        bigint not null,
    updated_at        bigint not null,
    deleted_at        bigint,
    server_updated_at bigint not null default 0,
    unique (cookbook_id, user_id)
);

-- Invite codes are only ever touched by the security-definer functions below.
create table if not exists public.cookbook_invites (
    code        text primary key,
    cookbook_id uuid        not null references public.cookbooks (id) on delete cascade,
    created_by  uuid        not null references auth.users (id) on delete cascade,
    created_at  timestamptz not null default now(),
    expires_at  timestamptz not null,
    used_by     uuid references auth.users (id) on delete set null,
    used_at     timestamptz
);

create table if not exists public.categories (
    id                uuid primary key,
    cookbook_id       uuid    not null references public.cookbooks (id) on delete cascade,
    name              text    not null,
    icon_key          text    not null,
    color_key         text    not null,
    position          integer not null,
    created_at        bigint  not null,
    updated_at        bigint  not null,
    deleted_at        bigint,
    server_updated_at bigint  not null default 0
);

create table if not exists public.recipes (
    id                uuid primary key,
    cookbook_id       uuid    not null references public.cookbooks (id) on delete cascade,
    title             text    not null,
    type              text,
    description       text    not null default '',
    servings          text    not null default '',
    prep_minutes      integer,
    cook_minutes      integer,
    source_url        text    not null default '',
    tags              text    not null default '',
    is_favorite       boolean not null default false,
    rating            integer,
    notes             text    not null default '',
    image_source      text    not null default 'FALLBACK',
    created_at        bigint  not null,
    updated_at        bigint  not null,
    deleted_at        bigint,
    server_updated_at bigint  not null default 0
);

create table if not exists public.recipe_categories (
    recipe_id         uuid   not null references public.recipes (id) on delete cascade,
    category_id       uuid   not null references public.categories (id) on delete cascade,
    cookbook_id       uuid   not null references public.cookbooks (id) on delete cascade,
    created_at        bigint not null,
    updated_at        bigint not null,
    deleted_at        bigint,
    server_updated_at bigint not null default 0,
    primary key (recipe_id, category_id)
);

create table if not exists public.ingredients (
    id                uuid primary key,
    recipe_id         uuid    not null references public.recipes (id) on delete cascade,
    cookbook_id       uuid    not null references public.cookbooks (id) on delete cascade,
    position          integer not null,
    section           text,
    quantity          text,
    unit              text,
    name              text    not null,
    note              text,
    created_at        bigint  not null,
    updated_at        bigint  not null,
    deleted_at        bigint,
    server_updated_at bigint  not null default 0
);

create table if not exists public.steps (
    id                uuid primary key,
    recipe_id         uuid    not null references public.recipes (id) on delete cascade,
    cookbook_id       uuid    not null references public.cookbooks (id) on delete cascade,
    position          integer not null,
    text              text    not null,
    timer_seconds     integer,
    created_at        bigint  not null,
    updated_at        bigint  not null,
    deleted_at        bigint,
    server_updated_at bigint  not null default 0
);

create table if not exists public.photos (
    id                     uuid primary key,
    recipe_id              uuid    not null references public.recipes (id) on delete cascade,
    cookbook_id            uuid    not null references public.cookbooks (id) on delete cascade,
    storage_path           text,
    thumbnail_storage_path text,
    width                  integer not null,
    height                 integer not null,
    is_cover               boolean not null default false,
    caption                text    not null default '',
    position               integer not null,
    created_at             bigint  not null,
    updated_at             bigint  not null,
    deleted_at             bigint,
    server_updated_at      bigint  not null default 0
);

create table if not exists public.cook_logs (
    id                uuid primary key,
    recipe_id         uuid   not null references public.recipes (id) on delete cascade,
    cookbook_id       uuid   not null references public.cookbooks (id) on delete cascade,
    made_on           bigint not null,
    rating            integer,
    notes             text   not null default '',
    created_at        bigint not null,
    updated_at        bigint not null,
    deleted_at        bigint,
    server_updated_at bigint not null default 0
);

create table if not exists public.cook_log_photos (
    id                     uuid primary key,
    cook_log_id            uuid    not null references public.cook_logs (id) on delete cascade,
    cookbook_id            uuid    not null references public.cookbooks (id) on delete cascade,
    storage_path           text,
    thumbnail_storage_path text,
    width                  integer not null,
    height                 integer not null,
    caption                text    not null default '',
    position               integer not null,
    created_at             bigint  not null,
    updated_at             bigint  not null,
    deleted_at             bigint,
    server_updated_at      bigint  not null default 0
);

-- Delta pulls filter by cookbook and order by server_updated_at.
create index if not exists cookbook_members_sync  on public.cookbook_members  (cookbook_id, server_updated_at);
create index if not exists cookbook_members_user  on public.cookbook_members  (user_id);
create index if not exists categories_sync        on public.categories        (cookbook_id, server_updated_at);
create index if not exists recipes_sync           on public.recipes           (cookbook_id, server_updated_at);
create index if not exists recipe_categories_sync on public.recipe_categories (cookbook_id, server_updated_at);
create index if not exists ingredients_sync       on public.ingredients       (cookbook_id, server_updated_at);
create index if not exists steps_sync             on public.steps             (cookbook_id, server_updated_at);
create index if not exists photos_sync            on public.photos            (cookbook_id, server_updated_at);
create index if not exists cook_logs_sync         on public.cook_logs         (cookbook_id, server_updated_at);
create index if not exists cook_log_photos_sync   on public.cook_log_photos   (cookbook_id, server_updated_at);

-- ---------------------------------------------------------------------------------------------
-- Last write wins + server_updated_at
-- ---------------------------------------------------------------------------------------------

create or replace function public.sync_stamp()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
    if tg_op = 'UPDATE' and new.updated_at < old.updated_at then
        -- An older edit arriving late never overwrites a newer one. Returning null skips the row,
        -- so an upsert of stale data is a no-op and the phone picks up the newer row on its pull.
        return null;
    end if;
    new.server_updated_at := (extract(epoch from clock_timestamp()) * 1000)::bigint;
    return new;
end;
$$;

do $$
declare
    t text;
begin
    foreach t in array array[
        'cookbooks', 'cookbook_members', 'categories', 'recipes', 'recipe_categories',
        'ingredients', 'steps', 'photos', 'cook_logs', 'cook_log_photos'
    ] loop
        execute format('drop trigger if exists sync_stamp on public.%I', t);
        execute format(
            'create trigger sync_stamp before insert or update on public.%I '
            'for each row execute function public.sync_stamp()', t);
    end loop;
end;
$$;

-- ---------------------------------------------------------------------------------------------
-- Membership helpers
-- ---------------------------------------------------------------------------------------------

-- Security definer so policies can call it without recursing into cookbook_members' own policy.
create or replace function public.is_member(p_cookbook_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select exists (
        select 1 from public.cookbook_members m
        where m.cookbook_id = p_cookbook_id
          and m.user_id = (select auth.uid())
          and m.deleted_at is null
    );
$$;

-- For storage paths like "<cookbook_id>/<photo_id>.jpg": compares as text, so a malformed path
-- is simply "not a member" instead of a uuid cast error.
create or replace function public.is_member_of_path(p_path text)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select exists (
        select 1 from public.cookbook_members m
        where m.cookbook_id::text = (storage.foldername(p_path))[1]
          and m.user_id = (select auth.uid())
          and m.deleted_at is null
    );
$$;

revoke all on function public.is_member(uuid) from public, anon;
revoke all on function public.is_member_of_path(text) from public, anon;
grant execute on function public.is_member(uuid) to authenticated;
grant execute on function public.is_member_of_path(text) to authenticated;

-- ---------------------------------------------------------------------------------------------
-- Row Level Security: on for every table; only members of a cook book can see or change it.
-- There are no DELETE policies: the app only soft-deletes.
-- ---------------------------------------------------------------------------------------------

alter table public.cookbooks         enable row level security;
alter table public.cookbook_members  enable row level security;
alter table public.cookbook_invites  enable row level security;
alter table public.categories        enable row level security;
alter table public.recipes           enable row level security;
alter table public.recipe_categories enable row level security;
alter table public.ingredients       enable row level security;
alter table public.steps             enable row level security;
alter table public.photos            enable row level security;
alter table public.cook_logs         enable row level security;
alter table public.cook_log_photos   enable row level security;

-- cookbooks: created through create_cookbook(); members can read and rename.
drop policy if exists "members read"   on public.cookbooks;
drop policy if exists "members insert" on public.cookbooks;
drop policy if exists "members update" on public.cookbooks;
create policy "members read"   on public.cookbooks for select to authenticated using (public.is_member(id));
create policy "members insert" on public.cookbooks for insert to authenticated with check (public.is_member(id));
create policy "members update" on public.cookbooks for update to authenticated
    using (public.is_member(id)) with check (public.is_member(id));

-- cookbook_members: read-only to members; rows are written by create_cookbook() / join_cookbook().
drop policy if exists "members read" on public.cookbook_members;
create policy "members read" on public.cookbook_members for select to authenticated
    using (public.is_member(cookbook_id));

-- cookbook_invites: RLS on with no policies, so only the functions below can touch it.

do $$
declare
    t text;
begin
    foreach t in array array[
        'categories', 'recipes', 'recipe_categories', 'ingredients', 'steps',
        'photos', 'cook_logs', 'cook_log_photos'
    ] loop
        execute format('drop policy if exists "members read" on public.%I', t);
        execute format('drop policy if exists "members insert" on public.%I', t);
        execute format('drop policy if exists "members update" on public.%I', t);
        execute format(
            'create policy "members read" on public.%I for select to authenticated '
            'using (public.is_member(cookbook_id))', t);
        execute format(
            'create policy "members insert" on public.%I for insert to authenticated '
            'with check (public.is_member(cookbook_id))', t);
        execute format(
            'create policy "members update" on public.%I for update to authenticated '
            'using (public.is_member(cookbook_id)) with check (public.is_member(cookbook_id))', t);
    end loop;
end;
$$;

-- ---------------------------------------------------------------------------------------------
-- Household functions (called from the app with the signed-in user's token)
-- ---------------------------------------------------------------------------------------------

-- Publishes the phone's local cook book (same id) and makes the caller its owner. Idempotent.
create or replace function public.create_cookbook(p_id uuid, p_name text, p_created_at bigint)
returns uuid
language plpgsql
security definer
set search_path = ''
as $$
declare
    uid uuid := auth.uid();
    now_ms bigint := (extract(epoch from clock_timestamp()) * 1000)::bigint;
begin
    if uid is null then
        raise exception 'Not signed in' using errcode = '28000';
    end if;

    if exists (select 1 from public.cookbooks where id = p_id) then
        if not public.is_member(p_id) then
            raise exception 'Not a member of this cook book' using errcode = '42501';
        end if;
        return p_id;
    end if;

    insert into public.cookbooks (id, name, created_at, updated_at)
    values (p_id, p_name, p_created_at, now_ms);

    insert into public.cookbook_members (cookbook_id, user_id, role, created_at, updated_at)
    values (p_id, uid, 'owner', now_ms, now_ms);

    return p_id;
end;
$$;

-- Returns a single-use invite code (e.g. "7K2QD-M9XRA") that expires in 7 days.
create or replace function public.create_invite(p_cookbook_id uuid)
returns text
language plpgsql
security definer
set search_path = ''
as $$
declare
    uid uuid := auth.uid();
    alphabet constant text := 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'; -- no 0/O or 1/I
    raw bytea;
    v_code text := '';
    i int;
begin
    if uid is null then
        raise exception 'Not signed in' using errcode = '28000';
    end if;
    if not public.is_member(p_cookbook_id) then
        raise exception 'Not a member of this cook book' using errcode = '42501';
    end if;

    loop
        raw := decode(replace(gen_random_uuid()::text, '-', ''), 'hex');
        v_code := '';
        for i in 0..9 loop
            v_code := v_code || substr(alphabet, (get_byte(raw, i) % 32) + 1, 1);
        end loop;
        exit when not exists (select 1 from public.cookbook_invites c where c.code = v_code);
    end loop;

    insert into public.cookbook_invites (code, cookbook_id, created_by, expires_at)
    values (v_code, p_cookbook_id, uid, now() + interval '7 days');

    return substr(v_code, 1, 5) || '-' || substr(v_code, 6, 5);
end;
$$;

-- Joins the cook book an unused, unexpired invite points at. Returns that cook book's id.
create or replace function public.join_cookbook(p_code text)
returns uuid
language plpgsql
security definer
set search_path = ''
as $$
declare
    uid uuid := auth.uid();
    normalized text := upper(regexp_replace(coalesce(p_code, ''), '[^A-Za-z0-9]', '', 'g'));
    invite public.cookbook_invites%rowtype;
    now_ms bigint := (extract(epoch from clock_timestamp()) * 1000)::bigint;
begin
    if uid is null then
        raise exception 'Not signed in' using errcode = '28000';
    end if;

    select * into invite from public.cookbook_invites
    where code = normalized and used_at is null and expires_at > now()
    for update;

    if not found then
        raise exception 'That invite code is not valid or has expired' using errcode = 'P0002';
    end if;

    insert into public.cookbook_members (cookbook_id, user_id, role, created_at, updated_at)
    values (invite.cookbook_id, uid, 'member', now_ms, now_ms)
    on conflict (cookbook_id, user_id) do update set deleted_at = null, updated_at = excluded.updated_at;

    update public.cookbook_invites set used_by = uid, used_at = now() where code = normalized;

    return invite.cookbook_id;
end;
$$;

revoke all on function public.create_cookbook(uuid, text, bigint) from public, anon;
revoke all on function public.create_invite(uuid) from public, anon;
revoke all on function public.join_cookbook(text) from public, anon;
grant execute on function public.create_cookbook(uuid, text, bigint) to authenticated;
grant execute on function public.create_invite(uuid) to authenticated;
grant execute on function public.join_cookbook(text) to authenticated;

-- ---------------------------------------------------------------------------------------------
-- Storage: a private bucket. Objects live at "<cookbook_id>/<photo_id>.jpg" and
-- "<cookbook_id>/thumbs/<photo_id>.jpg"; only members of that cook book can read or write them.
-- ---------------------------------------------------------------------------------------------

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('photos', 'photos', false, 10485760, array['image/jpeg'])
on conflict (id) do update set public = false;

drop policy if exists "cookbook photos: members read"   on storage.objects;
drop policy if exists "cookbook photos: members insert" on storage.objects;
drop policy if exists "cookbook photos: members update" on storage.objects;
create policy "cookbook photos: members read" on storage.objects for select to authenticated
    using (bucket_id = 'photos' and public.is_member_of_path(name));
create policy "cookbook photos: members insert" on storage.objects for insert to authenticated
    with check (bucket_id = 'photos' and public.is_member_of_path(name));
create policy "cookbook photos: members update" on storage.objects for update to authenticated
    using (bucket_id = 'photos' and public.is_member_of_path(name))
    with check (bucket_id = 'photos' and public.is_member_of_path(name));

-- ---------------------------------------------------------------------------------------------
-- Realtime: phones listen for changes and pull. Realtime applies the same RLS policies.
-- ---------------------------------------------------------------------------------------------

do $$
declare
    t text;
begin
    foreach t in array array[
        'cookbooks', 'cookbook_members', 'categories', 'recipes', 'recipe_categories',
        'ingredients', 'steps', 'photos', 'cook_logs', 'cook_log_photos'
    ] loop
        if not exists (
            select 1 from pg_publication_tables
            where pubname = 'supabase_realtime' and schemaname = 'public' and tablename = t
        ) then
            execute format('alter publication supabase_realtime add table public.%I', t);
        end if;
    end loop;
end;
$$;
