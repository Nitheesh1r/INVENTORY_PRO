package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myapplication.Product;
import com.example.myapplication.Transaction;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    // Products Table
    private static final String TABLE_PRODUCTS = "products";
    private static final String COL_PRODUCT_ID = "id";
    private static final String COL_PRODUCT_NAME = "name";
    private static final String COL_PRODUCT_SKU = "sku";
    private static final String COL_PRODUCT_CATEGORY = "category";
    private static final String COL_PRODUCT_QUANTITY = "quantity";
    private static final String COL_PRODUCT_MIN_STOCK = "min_stock";
    private static final String COL_PRODUCT_PRICE = "price";
    private static final String COL_PRODUCT_SUPPLIER = "supplier";
    private static final String COL_PRODUCT_CREATED = "created_at";

    // Transactions Table
    private static final String TABLE_TRANSACTIONS = "transactions";
    private static final String COL_TRANS_ID = "id";
    private static final String COL_TRANS_PRODUCT_ID = "product_id";
    private static final String COL_TRANS_PRODUCT_NAME = "product_name";
    private static final String COL_TRANS_TYPE = "type";
    private static final String COL_TRANS_QUANTITY = "quantity";
    private static final String COL_TRANS_NOTES = "notes";
    private static final String COL_TRANS_TIMESTAMP = "timestamp";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createProductsTable = "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_ID + " TEXT PRIMARY KEY, " +
                COL_PRODUCT_NAME + " TEXT NOT NULL, " +
                COL_PRODUCT_SKU + " TEXT UNIQUE NOT NULL, " +
                COL_PRODUCT_CATEGORY + " TEXT, " +
                COL_PRODUCT_QUANTITY + " INTEGER DEFAULT 0, " +
                COL_PRODUCT_MIN_STOCK + " INTEGER DEFAULT 0, " +
                COL_PRODUCT_PRICE + " REAL DEFAULT 0, " +
                COL_PRODUCT_SUPPLIER + " TEXT, " +
                COL_PRODUCT_CREATED + " INTEGER)";

        String createTransactionsTable = "CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                COL_TRANS_ID + " TEXT PRIMARY KEY, " +
                COL_TRANS_PRODUCT_ID + " TEXT NOT NULL, " +
                COL_TRANS_PRODUCT_NAME + " TEXT, " +
                COL_TRANS_TYPE + " TEXT NOT NULL, " +
                COL_TRANS_QUANTITY + " INTEGER NOT NULL, " +
                COL_TRANS_NOTES + " TEXT, " +
                COL_TRANS_TIMESTAMP + " INTEGER, " +
                "FOREIGN KEY(" + COL_TRANS_PRODUCT_ID + ") REFERENCES " +
                TABLE_PRODUCTS + "(" + COL_PRODUCT_ID + "))";

        db.execSQL(createProductsTable);
        db.execSQL(createTransactionsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
    }

    // ================= PRODUCT OPERATIONS =================

    public long addProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_PRODUCT_ID, product.getId());
        values.put(COL_PRODUCT_NAME, product.getName());
        values.put(COL_PRODUCT_SKU, product.getSku());
        values.put(COL_PRODUCT_CATEGORY, product.getCategory());
        values.put(COL_PRODUCT_QUANTITY, product.getQuantity());
        values.put(COL_PRODUCT_MIN_STOCK, product.getMinStock());
        values.put(COL_PRODUCT_PRICE, product.getPrice());
        values.put(COL_PRODUCT_SUPPLIER, product.getSupplier());
        values.put(COL_PRODUCT_CREATED, product.getCreatedAt());

        long result = db.insert(TABLE_PRODUCTS, null, values);
        db.close();
        return result;
    }

    public int updateProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_PRODUCT_NAME, product.getName());
        values.put(COL_PRODUCT_SKU, product.getSku());
        values.put(COL_PRODUCT_CATEGORY, product.getCategory());
        values.put(COL_PRODUCT_QUANTITY, product.getQuantity());
        values.put(COL_PRODUCT_MIN_STOCK, product.getMinStock());
        values.put(COL_PRODUCT_PRICE, product.getPrice());
        values.put(COL_PRODUCT_SUPPLIER, product.getSupplier());

        int result = db.update(TABLE_PRODUCTS, values,
                COL_PRODUCT_ID + " = ?", new String[]{product.getId()});
        db.close();
        return result;
    }

    public void deleteProduct(String productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PRODUCTS, COL_PRODUCT_ID + " = ?", new String[]{productId});
        db.delete(TABLE_TRANSACTIONS, COL_TRANS_PRODUCT_ID + " = ?", new String[]{productId});
        db.close();
    }

    public Product getProduct(String productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null,
                COL_PRODUCT_ID + " = ?", new String[]{productId},
                null, null, null);

        Product product = null;
        if (cursor != null && cursor.moveToFirst()) {
            product = cursorToProduct(cursor);
            cursor.close();
        }
        db.close();
        return product;
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null, null, null,
                null, null, COL_PRODUCT_NAME + " ASC");

        if (cursor.moveToFirst()) {
            do {
                products.add(cursorToProduct(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return products;
    }

    public List<Product> searchProducts(String query) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COL_PRODUCT_NAME + " LIKE ? OR " +
                COL_PRODUCT_SKU + " LIKE ? OR " +
                COL_PRODUCT_CATEGORY + " LIKE ?";

        String[] selectionArgs = new String[]{
                "%" + query + "%", "%" + query + "%", "%" + query + "%"};

        Cursor cursor = db.query(TABLE_PRODUCTS, null, selection, selectionArgs,
                null, null, COL_PRODUCT_NAME + " ASC");

        if (cursor.moveToFirst()) {
            do {
                products.add(cursorToProduct(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return products;
    }

    public List<Product> getLowStockProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COL_PRODUCT_QUANTITY + " <= " + COL_PRODUCT_MIN_STOCK;

        Cursor cursor = db.query(TABLE_PRODUCTS, null, selection, null,
                null, null, COL_PRODUCT_QUANTITY + " ASC");

        if (cursor.moveToFirst()) {
            do {
                products.add(cursorToProduct(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return products;
    }

    private Product cursorToProduct(Cursor cursor) {
        Product product = new Product();
        product.setId(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_ID)));
        product.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_NAME)));
        product.setSku(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_SKU)));
        product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_CATEGORY)));
        product.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_QUANTITY)));
        product.setMinStock(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_MIN_STOCK)));
        product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRODUCT_PRICE)));
        product.setSupplier(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_SUPPLIER)));
        product.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COL_PRODUCT_CREATED)));
        return product;
    }

    // ================= TRANSACTION OPERATIONS =================

    public long addTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_TRANS_ID, transaction.getId());
        values.put(COL_TRANS_PRODUCT_ID, transaction.getProductId());
        values.put(COL_TRANS_PRODUCT_NAME, transaction.getProductName());
        values.put(COL_TRANS_TYPE, transaction.getType());
        values.put(COL_TRANS_QUANTITY, transaction.getQuantity());
        values.put(COL_TRANS_NOTES, transaction.getNotes());
        values.put(COL_TRANS_TIMESTAMP, transaction.getTimestamp());

        long result = db.insert(TABLE_TRANSACTIONS, null, values);
        db.close();
        return result;
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TRANSACTIONS, null, null, null,
                null, null, COL_TRANS_TIMESTAMP + " DESC");

        if (cursor.moveToFirst()) {
            do {
                transactions.add(cursorToTransaction(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return transactions;
    }

    public List<Transaction> getProductTransactions(String productId) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TRANSACTIONS, null,
                COL_TRANS_PRODUCT_ID + " = ?", new String[]{productId},
                null, null, COL_TRANS_TIMESTAMP + " DESC");

        if (cursor.moveToFirst()) {
            do {
                transactions.add(cursorToTransaction(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return transactions;
    }

    private Transaction cursorToTransaction(Cursor cursor) {
        Transaction transaction = new Transaction();
        transaction.setId(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_ID)));
        transaction.setProductId(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_PRODUCT_ID)));
        transaction.setProductName(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_PRODUCT_NAME)));
        transaction.setType(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_TYPE)));
        transaction.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRANS_QUANTITY)));
        transaction.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_NOTES)));
        transaction.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COL_TRANS_TIMESTAMP)));
        return transaction;
    }

    // ================= STATISTICS =================

    public int getTotalProductCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PRODUCTS, null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }

    public int getTotalItemCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COL_PRODUCT_QUANTITY + ") FROM " + TABLE_PRODUCTS, null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }

    public double getTotalInventoryValue() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT SUM(" +
                COL_PRODUCT_QUANTITY + " * " + COL_PRODUCT_PRICE + ") FROM " +
                TABLE_PRODUCTS, null);

        double value = 0;
        if (cursor.moveToFirst()) value = cursor.getDouble(0);

        cursor.close();
        db.close();
        return value;
    }
}
