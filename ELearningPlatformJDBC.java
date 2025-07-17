import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ELearningPlatformJDBC {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/elearning";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    public static void main(String[] args) {
        try {
            // Initialize database connection
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
            // Create database tables if they don't exist
            createTables(connection);
            
            // Create platform instance
            Platform platform = new Platform(connection, "LearnHub");
            
            // Create and add instructors
            Instructor instructor1 = new Instructor(connection, 1, "Dr. Smith", "smith@example.com");
            Instructor instructor2 = new Instructor(connection, 2, "Prof. Johnson", "johnson@example.com");
            platform.addInstructor(instructor1);
            platform.addInstructor(instructor2);
            
            // Create and add courses
            Course programmingCourse = new Course(connection, "C101", "Java Programming", 
                                              "Learn Java from scratch", 1);
            Course mathCourse = new Course(connection, "M201", "Advanced Mathematics", 
                                        "Advanced math concepts", 2);
            platform.addCourse(programmingCourse);
            platform.addCourse(mathCourse);
            
            // Add modules to courses
            programmingCourse.addModule(new Module(connection, "Introduction to Java", "Basic syntax and concepts", "C101"));
            programmingCourse.addModule(new Module(connection, "OOP in Java", "Object-oriented programming", "C101"));
            programmingCourse.addModule(new Module(connection, "Java Collections", "Working with collections", "C101"));
            
            mathCourse.addModule(new Module(connection, "Linear Algebra", "Vectors and matrices", "M201"));
            mathCourse.addModule(new Module(connection, "Calculus", "Derivatives and integrals", "M201"));
            
            // Add quizzes to modules
            programmingCourse.getModules().get(0).addQuiz(new Quiz(connection, "Java Basics Quiz", 10, 1));
            programmingCourse.getModules().get(1).addQuiz(new Quiz(connection, "OOP Quiz", 15, 2));
            
            // Create and add students
            Student student1 = new Student(connection, 101, "Alice", "alice@example.com");
            Student student2 = new Student(connection, 102, "Bob", "bob@example.com");
            platform.addStudent(student1);
            platform.addStudent(student2);
            
            // Enroll students in courses
            platform.enrollStudent(student1, programmingCourse);
            platform.enrollStudent(student2, programmingCourse);
            platform.enrollStudent(student1, mathCourse);
            
            // Track progress
            student1.completeModule(programmingCourse, programmingCourse.getModules().get(0));
            student1.takeQuiz(programmingCourse.getModules().get(0).getQuizzes().get(0), 8);
            
            // Display platform information
            displayPlatformInfo(platform, student1);
            
            // Close connection
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static void createTables(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create tables if they don't exist
            stmt.execute("CREATE TABLE IF NOT EXISTS instructors (" +
                         "id INT PRIMARY KEY, " +
                         "name VARCHAR(100), " +
                         "email VARCHAR(100))");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS courses (" +
                         "course_id VARCHAR(10) PRIMARY KEY, " +
                         "title VARCHAR(100), " +
                         "description TEXT, " +
                         "instructor_id INT, " +
                         "FOREIGN KEY (instructor_id) REFERENCES instructors(id))");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS modules (" +
                         "module_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "title VARCHAR(100), " +
                         "description TEXT, " +
                         "course_id VARCHAR(10), " +
                         "FOREIGN KEY (course_id) REFERENCES courses(course_id))");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS quizzes (" +
                         "quiz_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "name VARCHAR(100), " +
                         "total_points INT, " +
                         "module_id INT, " +
                         "FOREIGN KEY (module_id) REFERENCES modules(module_id))");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                         "id INT PRIMARY KEY, " +
                         "name VARCHAR(100), " +
                         "email VARCHAR(100))");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS enrollments (" +
                         "student_id INT, " +
                         "course_id VARCHAR(10), " +
                         "PRIMARY KEY (student_id, course_id), " +
                         "FOREIGN KEY (student_id) REFERENCES students(id), " +
                         "FOREIGN KEY (course_id) REFERENCES courses(course_id))");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS completed_modules (" +
                         "student_id INT, " +
                         "module_id INT, " +
                         "PRIMARY KEY (student_id, module_id), " +
                         "FOREIGN KEY (student_id) REFERENCES students(id), " +
                         "FOREIGN KEY (module_id) REFERENCES modules(module_id))");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS quiz_results (" +
                         "result_id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "student_id INT, " +
                         "quiz_id INT, " +
                         "score INT, " +
                         "FOREIGN KEY (student_id) REFERENCES students(id), " +
                         "FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id))");
        }
    }
    
    private static void displayPlatformInfo(Platform platform, Student student) throws SQLException {
        System.out.println("Platform: " + platform.getName());
        System.out.println("\nCourses:");
        for (Course course : platform.getCourses()) {
            System.out.println("- " + course.getTitle() + " (" + course.getEnrolledStudentsCount() + " students)");
        }
        
        System.out.println("\nStudent Progress:");
        System.out.println(student.getName() + "'s progress in Java Programming: " + 
            platform.getStudentProgress(student, platform.getCourses().get(0)) + "%");
        
        System.out.println("\nQuiz Results:");
        for (QuizResult result : student.getQuizResults()) {
            System.out.println("- " + result.getQuizName() + ": " + 
                result.getScore() + "/" + result.getTotalPoints());
        }
    }
}

class Platform {
    private Connection connection;
    private String name;
    private List<Course> courses;
    private List<Student> students;
    private List<Instructor> instructors;
    
    public Platform(Connection connection, String name) {
        this.connection = connection;
        this.name = name;
        this.courses = new ArrayList<>();
        this.students = new ArrayList<>();
        this.instructors = new ArrayList<>();
    }
    
    public String getName() { return name; }
    public List<Course> getCourses() { return courses; }
    public List<Student> getStudents() { return students; }
    public List<Instructor> getInstructors() { return instructors; }
    
    public void addCourse(Course course) { courses.add(course); }
    public void addStudent(Student student) { students.add(student); }
    public void addInstructor(Instructor instructor) { instructors.add(instructor); }
    
    public void enrollStudent(Student student, Course course) throws SQLException {
        if (!isStudentEnrolled(student.getId(), course.getCourseId())) {
            String sql = "INSERT INTO enrollments (student_id, course_id) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, student.getId());
                stmt.setString(2, course.getCourseId());
                stmt.executeUpdate();
            }
            course.incrementEnrolledStudents();
            student.getEnrolledCourseIds().add(course.getCourseId());
        }
    }
    
    public double getStudentProgress(Student student, Course course) throws SQLException {
        String sql = "SELECT COUNT(*) FROM completed_modules cm " +
                     "JOIN modules m ON cm.module_id = m.module_id " +
                     "WHERE cm.student_id = ? AND m.course_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, student.getId());
            stmt.setString(2, course.getCourseId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int completed = rs.getInt(1);
                int total = course.getModules().size();
                return total > 0 ? (double) completed / total * 100 : 0;
            }
        }
        return 0;
    }
    
    private boolean isStudentEnrolled(int studentId, String courseId) throws SQLException {
        String sql = "SELECT 1 FROM enrollments WHERE student_id = ? AND course_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setString(2, courseId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }
}

class User {
    protected Connection connection;
    protected int id;
    protected String name;
    protected String email;
    
    public User(Connection connection, int id, String name, String email) {
        this.connection = connection;
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}

class Instructor extends User {
    public Instructor(Connection connection, int id, String name, String email) throws SQLException {
        super(connection, id, name, email);
        saveToDatabase();
    }
    
    private void saveToDatabase() throws SQLException {
        String sql = "INSERT INTO instructors (id, name, email) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE name = VALUES(name), email = VALUES(email)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, name);
            stmt.setString(3, email);
            stmt.executeUpdate();
        }
    }
}

class Student extends User {
    private List<String> enrolledCourseIds;
    private List<QuizResult> quizResults;
    
    public Student(Connection connection, int id, String name, String email) throws SQLException {
        super(connection, id, name, email);
        this.enrolledCourseIds = new ArrayList<>();
        this.quizResults = new ArrayList<>();
        saveToDatabase();
        loadQuizResults();
    }
    
    public List<String> getEnrolledCourseIds() { return enrolledCourseIds; }
    public List<QuizResult> getQuizResults() { return quizResults; }
    
    private void saveToDatabase() throws SQLException {
        String sql = "INSERT INTO students (id, name, email) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE name = VALUES(name), email = VALUES(email)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, name);
            stmt.setString(3, email);
            stmt.executeUpdate();
        }
    }
    
    public void completeModule(Course course, Module module) throws SQLException {
        String sql = "INSERT INTO completed_modules (student_id, module_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, module.getModuleId());
            stmt.executeUpdate();
        }
    }
    
    public void takeQuiz(Quiz quiz, int score) throws SQLException {
        String sql = "INSERT INTO quiz_results (student_id, quiz_id, score) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, quiz.getQuizId());
            stmt.setInt(3, score);
            stmt.executeUpdate();
        }
        quizResults.add(new QuizResult(quiz.getName(), quiz.getTotalPoints(), score));
    }
    
    private void loadQuizResults() throws SQLException {
        String sql = "SELECT q.name, q.total_points, qr.score FROM quiz_results qr " +
                     "JOIN quizzes q ON qr.quiz_id = q.quiz_id " +
                     "WHERE qr.student_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                quizResults.add(new QuizResult(
                    rs.getString("name"),
                    rs.getInt("total_points"),
                    rs.getInt("score")
                ));
            }
        }
    }
}

class Course {
    private Connection connection;
    private String courseId;
    private String title;
    private String description;
    private int instructorId;
    private List<Module> modules;
    private int enrolledStudentsCount;
    
    public Course(Connection connection, String courseId, String title, 
                 String description, int instructorId) throws SQLException {
        this.connection = connection;
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.instructorId = instructorId;
        this.modules = new ArrayList<>();
        this.enrolledStudentsCount = 0;
        saveToDatabase();
    }
    
    public String getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getInstructorId() { return instructorId; }
    public List<Module> getModules() { return modules; }
    public int getEnrolledStudentsCount() { return enrolledStudentsCount; }
    
    public void incrementEnrolledStudents() { enrolledStudentsCount++; }
    
    private void saveToDatabase() throws SQLException {
        String sql = "INSERT INTO courses (course_id, title, description, instructor_id) " +
                     "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                     "title = VALUES(title), description = VALUES(description), instructor_id = VALUES(instructor_id)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.setInt(4, instructorId);
            stmt.executeUpdate();
        }
    }
    
    public void addModule(Module module) {
        modules.add(module);
    }
}

class Module {
    private Connection connection;
    private int moduleId;
    private String title;
    private String description;
    private String courseId;
    private List<Quiz> quizzes;
    
    public Module(Connection connection, String title, String description, String courseId) throws SQLException {
        this.connection = connection;
        this.title = title;
        this.description = description;
        this.courseId = courseId;
        this.quizzes = new ArrayList<>();
        saveToDatabase();
    }
    
    public int getModuleId() { return moduleId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCourseId() { return courseId; }
    public List<Quiz> getQuizzes() { return quizzes; }
    
    private void saveToDatabase() throws SQLException {
        String sql = "INSERT INTO modules (title, description, course_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setString(3, courseId);
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                moduleId = rs.getInt(1);
            }
        }
    }
    
    public void addQuiz(Quiz quiz) {
        quizzes.add(quiz);
    }
}

class Quiz {
    private Connection connection;
    private int quizId;
    private String name;
    private int totalPoints;
    private int moduleId;
    
    public Quiz(Connection connection, String name, int totalPoints, int moduleId) throws SQLException {
        this.connection = connection;
        this.name = name;
        this.totalPoints = totalPoints;
        this.moduleId = moduleId;
        saveToDatabase();
    }
    
    public int getQuizId() { return quizId; }
    public String getName() { return name; }
    public int getTotalPoints() { return totalPoints; }
    public int getModuleId() { return moduleId; }
    
    private void saveToDatabase() throws SQLException {
        String sql = "INSERT INTO quizzes (name, total_points, module_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setInt(2, totalPoints);
            stmt.setInt(3, moduleId);
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                quizId = rs.getInt(1);
            }
        }
    }
}

class QuizResult {
    private String quizName;
    private int totalPoints;
    private int score;
    
    public QuizResult(String quizName, int totalPoints, int score) {
        this.quizName = quizName;
        this.totalPoints = totalPoints;
        this.score = score;
    }
    
    public String getQuizName() { return quizName; }
    public int getTotalPoints() { return totalPoints; }
    public int getScore() { return score; }
}
