package app.jscookbook.core.data.di

import android.content.Context
import androidx.room.Room
import app.jscookbook.core.data.db.CategoryDao
import app.jscookbook.core.data.db.CookbookDao
import app.jscookbook.core.data.db.JsCookBookDatabase
import app.jscookbook.core.data.repository.Clock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): JsCookBookDatabase =
        Room.databaseBuilder(context, JsCookBookDatabase::class.java, "jscookbook.db").build()

    @Provides
    fun provideCookbookDao(db: JsCookBookDatabase): CookbookDao = db.cookbookDao()

    @Provides
    fun provideCategoryDao(db: JsCookBookDatabase): CategoryDao = db.categoryDao()

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock { System.currentTimeMillis() }
}
