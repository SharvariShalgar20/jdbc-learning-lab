package com.Sharvari.libraryManagement;

import com.Sharvari.libraryManagement.dao.BookDAO;
import com.Sharvari.libraryManagement.model.Book;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        BookDAO dao = new BookDAO();

        while(true) {
            System.out.println("\n1. Add Book");
            System.out.println("2. View Books");
            System.out.println("3. Issue Book");
            System.out.println("4. Exit");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 :
                    System.out.print("Title: ");
                    String title = sc.nextLine();

                    System.out.print("Author: ");
                    String author = sc.nextLine();

                    dao.addBook(new Book(title, author));
                    break;

                case 2 :
                    List<Book> books = dao.getAllBooks();

                    books.forEach(b ->
                            System.out.println(b.getId() + " | " + b.getTitle() + " | " + b.getAuthor() + " | " + b.isAvailable()
                            ));
                    break;

                case 3 :
                    System.out.print("Enter Book ID: ");
                    int id = sc.nextInt();
                    dao.issueBook(id);
                    break;

                case 4:
                    System.exit(0);

            }
        }
    }
}
