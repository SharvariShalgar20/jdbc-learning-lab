package com.enrollment.menu;

import com.enrollment.dao.CourseDAO;
import com.enrollment.dao.EnrollmentDAO;
import com.enrollment.dao.StudentDAO;
import com.enrollment.model.Student;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class MenuHandler {

    private final Scanner sc = new Scanner(System.in);
    private final StudentDAO studentDAO = new StudentDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    public void start(){
        while (true){
            System.out.println("""
                \n========= ENROLLMENT PORTAL =========
                1. Student Management
                2. Course Management
                3. Enrollment Management
                0. Exit
                ======================================""");
            System.out.print("Choice: ");

            int choice = Integer.parseInt(sc.nextLine().trim());

            try {
                switch (choice) {
                    case 1 :
                        studentMenu();
                        break;

                    case 2 :
                        courseMenu();
                        break;

                    case 3 :
                        enrollmentMenu();
                        break;

                    case 0 :
                        System.out.println("Bye!"); return;

                    default:
                        System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void studentMenu() throws SQLException {
        System.out.println("""
            --- STUDENTS ---
            1. Add  2. View All  3. Update  4. Delete""");

        int c = Integer.parseInt(sc.nextLine().trim());

        switch (c) {

            case 1 :
                System.out.print("Name : ");
                String name = sc.nextLine();
                System.out.print("Email : ");
                String email = sc.nextLine();

                studentDAO.addStudent(name, email);
                System.out.println("Student Added!");
                break;

            case 2 :
                List<Student> list = studentDAO.getAllStudents();
                for (Student s : list) {
                    System.out.println("ID: " + s.getId() + " | Name: " + s.getName());
                }
                break;

            case 3 :
                System.out.print("Student ID to Update :");
                int id = Integer.parseInt(sc.nextLine().trim());
                System.out.print("New name :");
                String newName = sc.nextLine().trim();
                System.out.print("New Email :");
                String newEmail = sc.nextLine().trim();
                studentDAO.updateStudent(id, newName, newEmail);
                break;

            case 4 :
                System.out.print("Student ID to delete: ");
                int idToDelete = Integer.parseInt(sc.nextLine().trim());
                studentDAO.deleteStudent(idToDelete);
                break;

            default:
                System.out.println("Invalid option.");
        }
    }

    public void courseMenu() throws SQLException {

    }

    public void enrollmentMenu() throws SQLException {

    }
}
