package com.example.spendsmart.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.spendsmart.data.local.dao.BudgetDao;
import com.example.spendsmart.data.local.dao.CategoryBudgetDao;
import com.example.spendsmart.data.local.dao.CustomCategoryDao;
import com.example.spendsmart.data.local.dao.RecurringTransactionDao;
import com.example.spendsmart.data.local.dao.SavingsGoalDao;
import com.example.spendsmart.data.local.dao.SplitBillDao;
import com.example.spendsmart.data.local.dao.TransactionDao;
import com.example.spendsmart.data.local.dao.UserDao;
import com.example.spendsmart.data.local.entity.Budget;
import com.example.spendsmart.data.local.entity.CategoryBudget;
import com.example.spendsmart.data.local.entity.CustomCategory;
import com.example.spendsmart.data.local.entity.RecurringTransaction;
import com.example.spendsmart.data.local.entity.SavingsGoal;
import com.example.spendsmart.data.local.entity.SplitBill;
import com.example.spendsmart.data.local.entity.Transaction;
import com.example.spendsmart.data.local.entity.User;

@Database(
        entities = {
                User.class,
                Transaction.class,
                Budget.class,
                CustomCategory.class,
                CategoryBudget.class,
                RecurringTransaction.class,
                SavingsGoal.class,
                SplitBill.class
        },
        version = 5,
        exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract TransactionDao transactionDao();
    public abstract BudgetDao budgetDao();
    public abstract CustomCategoryDao customCategoryDao();
    public abstract CategoryBudgetDao categoryBudgetDao();
    public abstract RecurringTransactionDao recurringTransactionDao();
    public abstract SavingsGoalDao savingsGoalDao();
    public abstract SplitBillDao splitBillDao();

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE transactions ADD COLUMN receiptUri TEXT");
            database.execSQL("ALTER TABLE transactions ADD COLUMN recurringId INTEGER");
            database.execSQL("CREATE TABLE IF NOT EXISTS category_budgets ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "userId INTEGER NOT NULL, "
                    + "category TEXT NOT NULL, "
                    + "monthlyLimit REAL NOT NULL, "
                    + "FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_category_budgets_userId "
                    + "ON category_budgets(userId)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS "
                    + "index_category_budgets_userId_category "
                    + "ON category_budgets(userId, category)");
            database.execSQL("CREATE TABLE IF NOT EXISTS recurring_transactions ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "userId INTEGER NOT NULL, "
                    + "amount REAL NOT NULL, "
                    + "type TEXT, "
                    + "category TEXT, "
                    + "note TEXT, "
                    + "frequency TEXT, "
                    + "nextRunAt INTEGER NOT NULL, "
                    + "active INTEGER NOT NULL, "
                    + "FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_transactions_userId "
                    + "ON recurring_transactions(userId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_transactions_nextRunAt "
                    + "ON recurring_transactions(nextRunAt)");
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE transactions ADD COLUMN paymentMethod TEXT");
            database.execSQL("CREATE TABLE IF NOT EXISTS savings_goals ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "userId INTEGER NOT NULL, "
                    + "title TEXT NOT NULL, "
                    + "targetAmount REAL NOT NULL, "
                    + "savedAmount REAL NOT NULL, "
                    + "deadlineMillis INTEGER, "
                    + "FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_savings_goals_userId "
                    + "ON savings_goals(userId)");
            database.execSQL("CREATE TABLE IF NOT EXISTS split_bills ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "userId INTEGER NOT NULL, "
                    + "title TEXT NOT NULL, "
                    + "friendName TEXT NOT NULL, "
                    + "totalAmount REAL NOT NULL, "
                    + "yourShare REAL NOT NULL, "
                    + "friendOwesYou INTEGER NOT NULL, "
                    + "settled INTEGER NOT NULL, "
                    + "dateMillis INTEGER NOT NULL, "
                    + "FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_split_bills_userId "
                    + "ON split_bills(userId)");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "spendsmart.db"
                            )
                            .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
