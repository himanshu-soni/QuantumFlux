package info.quantumflux.sample;

import android.accounts.Account;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import info.quantumflux.QuantumFlux;
import info.quantumflux.QuantumFluxSyncHelper;
import info.quantumflux.model.query.Select;
import info.quantumflux.model.util.QuantumFluxBatchDispatcher;
import info.quantumflux.sample.data.Author;
import info.quantumflux.sample.data.Book;
import info.quantumflux.sample.data.Publisher;
import info.quantumflux.sample.sync.SyncConstants;
import info.quantumflux.sample.sync.SyncUtils;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    // Instance fields
    Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // For sync adapter
        mAccount = SyncUtils.createSyncAccount(this, "Dummy Account");
        SyncUtils.startPeriodicSync(mAccount);

        // delete all previous data
        QuantumFlux.deleteAll(Book.class);
        QuantumFlux.deleteAll(Author.class);
        QuantumFlux.deleteAll(Publisher.class);

        Book book = new Book();
        book.name = "Sorcerer's Stone";
        book.isbn = "122342564";

        Drawable drawable = getResources().getDrawable(R.drawable.hpsc);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        book.bookCover = stream.toByteArray();

        QuantumFlux.insert(book);

        Author author = new Author();
        author.name = "J.K. Rollings";
        author.save();

        Publisher publisher = new Publisher();
        publisher.name = "Bloomsbury";
        QuantumFlux.insert(publisher);


        ImageView cover = (ImageView) findViewById(R.id.cover);
        TextView bookName = (TextView) findViewById(R.id.book_name);
        TextView authorName = (TextView) findViewById(R.id.author_name);
        TextView pubName = (TextView) findViewById(R.id.publisher_name);

        Book firstBook = Select.from(Book.class).first();
        Author firstAuthor = Select.from(Author.class).first();
        Publisher firstPublisher = Select.from(Publisher.class).first();

        bookName.setText(firstBook.name);
        authorName.setText(firstAuthor.name);
        pubName.setText(firstPublisher.name);


        Bitmap decodedByte = BitmapFactory.decodeByteArray(firstBook.bookCover, 0, firstBook.bookCover.length);
        cover.setImageBitmap(decodedByte);

        // update
        Author first = Select.from(Author.class).first();
        first.name = "J. K. Rowling";
        first.update();

        authorName.setText(first.name);


        // update from QuantumFlux
        Book isbnBook = Select.from(Book.class).whereEquals("isbn", "122342564").first();
        isbnBook.isbn = "122342567";
        QuantumFlux.update(isbnBook);

        // batch insert example
        int batchSize = 100;
        QuantumFluxBatchDispatcher<Book> dispatcher = new QuantumFluxBatchDispatcher<Book>(this, Book.class, batchSize);
        for (int i = 0; i < batchSize; i++) {
            Book b = new Book();
            b.name = "Book " + i;
            b.isbn = "ISBN" + b.hashCode();
            dispatcher.add(b);
        }
        dispatcher.release(true);

        int count = Select.from(Book.class).queryAsCount();
        Toast.makeText(this, "Total Books : " + count, Toast.LENGTH_LONG).show();


        // To notify sync
        try {
            Book newBook = new Book();
            newBook.name = "Chamber of Secrets";
            QuantumFluxSyncHelper.insert(getContentResolver().acquireContentProviderClient(SyncConstants.AUTHORITY), newBook);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        SyncUtils.refreshManually(mAccount);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
