# QuantumFlux
A Powerful Android Content Provider ORM. It uses Content Providers to access the data and internally uses SQLite to actually store the data.
It can be used as a typical ORM, by creating Java objects and applying some Annotations and/or by extending `QuantumFluxRecord` on them and
they will be accessed normally and creates [ContentProvider](http://developer.android.com/reference/android/content/ContentProvider.html). 


[ ![Download](https://api.bintray.com/packages/himanshu-soni/maven/QuantumFlux/images/download.svg) ](https://bintray.com/himanshu-soni/maven/QuantumFlux/_latestVersion)


For more information in detail, please check [Wiki](https://github.com/himanshu-soni/QuantumFlux/wiki) page.

## Features

1. Create table from data modal objects, either by annotation or by extending `QuantumFluxRecord`.
2. Create table view from data modal.
3. Advanced query creation using `QueryBuilder`, complex queries can be created very easily.
4. Supports many common data types as Date, Calender, UUID, BigDecimal, and also allows custom column type mappings.
5. Table change listeners for views to auto reflect data changes.
6. Very easy to setup, use and query.

## Installation

#### Gradle (Recommended)

add following line to your module's dependency list:

```
compile 'info.quantumflux:library:0.9.0'
```


#### Maven

Declare the dependency in Maven:

```
<dependency>
    <groupId>info.quantumflux</groupId>
    <artifactId>library</artifactId>
    <version>0.9.0</version>
</dependency>
```

#### For eclipse users

You can also download the source and import as library project.

#### or add jar

See [releases](https://github.com/himanshu-soni/QuantumFlux/releases) page to download jar for latest version directly.

===================

## Basic Usage

1. Install the project via any of the method above. and extend `Application` and us it as following:
  
   ```
   public class SampleApplication extends Application {
       @Override
       protected void attachBaseContext(Context base) {
           super.attachBaseContext(base);
           QuantumFlux.initialize(this);
       }
   }
   ```
   
2. register this application class in Manifest file:
   
   ```xml
   <application
           android:name="info.quantumflux.sample.SampleApplication"
           ...
           >
   ```
   
   
3. following meta data in `<application>` tag
   
   ```xml
   <meta-data android:name="AUTHORITY" android:value="info.quantumflux.sample" />
   <meta-data android:name="DATABASE_NAME" android:value="QuantumFluxSample.sqlite" />
   <meta-data android:name="DATABASE_VERSION" android:value="1" />
   <meta-data android:name="PACKAGE_NAME" android:value="info.quantumflux.sample" />
   <meta-data android:name="QUERY_LOG" android:value="true" /> <!-- Optional -->
   ```
   
   
4. register content provider:
    
    ```xml
    <provider
        android:name="info.quantumflux.provider.QuantumFluxContentProvider"
        android:authorities="info.quantumflux.sample"
        android:exported="false" />
    ```
    
5. Now, you just have to create modal classes by:
    * annotation:
        
        ```
        @Table
        public class Book {
        ...
        }
        ```
    
    * extending class:
        
        ```
        public class Author extends QuantumFluxRecord<Author> {
            public String name;
            ...
        }
        ```

6. Access:
    
    ```
    Book book = new Book();
    book.name = "Sorcerer's Stone";
    book.isbn = "122342564";
    QuantumFlux.insert(book);
    ```
    
    ```
    Author author = new Author();
    author.name = "J.K. Rollings";
    author.save();
    ```
    
    ```
    Author first = Select.from(Author.class).first();
    first.name = "J. K. Rowling";
    first.update();
    ```
    
    ```
    QuantumFlux.deleteAll(Book.class);
    Author first = Select.from(Author.class).first();
    first.delete();
    ```

=========================

For detailed configuration and advance usages, go through
[https://github.com/himanshu-soni/QuantumFlux/wiki](https://github.com/himanshu-soni/QuantumFlux/wiki)


developed to make programming easy.

by Himanshu Soni (himanshu_soni@mail.com)
