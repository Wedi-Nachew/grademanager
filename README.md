# Grade Manager

A comprehensive grade management system built with JavaFX and MySQL, designed for educational institutions to manage student assessments, grades, and academic records.

## Features

- **User Management**
  - Multiple user roles (Admin, Teacher, Student)
  - Secure authentication system
  - Profile management for all users

- **Assessment Management**
  - Create and manage different types of assessments (Continuous, Final)
  - Support for multiple subjects and class levels
  - Flexible grading system with customizable total marks

- **Grade Management**
  - Real-time grade entry and updates
  - Grade calculation and aggregation
  - Performance tracking and analysis

- **Class Management**
  - Support for multiple class levels and sections
  - Stream and language options for higher grades
  - Teacher-class-subject assignments

## Technical Architecture

The application follows a well-structured architecture implementing several design patterns and principles:

### Design Patterns
- Repository Pattern for data access
- Service Layer Pattern for business logic
- MVC Pattern for UI organization
- Singleton Pattern for database connection
- Observer Pattern for real-time updates
- Strategy Pattern for assessment types
- Factory Pattern for object creation
- DTO Pattern for data transfer

### Design Principles
- SOLID Principles
- DRY (Don't Repeat Yourself)
- KISS (Keep It Simple, Stupid)
- Encapsulation
- Abstraction
- Separation of Concerns
- Dependency Injection
- Interface-based Programming
- Fail-Fast Principle
- Tell-Don't-Ask Principle

For detailed documentation of design patterns and principles, see [DESIGN_ANALYSIS.md](DESIGN_ANALYSIS.md).

## Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher

## Database Setup

1. Create a MySQL database named `grademanager`
2. The application will automatically create required tables on first run
3. Default database configuration:
   - URL: `jdbc:mysql://localhost:3306/grademanager`
   - Username: `root`
   - Password: `` (empty by default)

## Building and Running

1. Clone the repository
2. Navigate to the project directory
3. Build the project:
   ```bash
   mvn clean install
   ```
4. Run the application:
   ```bash
   mvn javafx:run
   ```

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── school/
│   │           └── grademanager/
│   │               ├── controller/    # UI Controllers
│   │               ├── model/         # Data Models
│   │               ├── repository/    # Data Access
│   │               ├── service/       # Business Logic
│   │               └── util/          # Utilities
│   └── resources/
│       └── view/     # FXML Views
```

## Contributors

### Development Team
- **Filmon Gebrelibanos** (UGR/170182/12)
  - Lead Developer
  - Database Design
  - Core Architecture Implementation

- **Tekle Beyene** (UGR/170122/12)
  - UI/UX Design
  - JavaFX Implementation
  - User Interface Development

- **Mehari Desta** (EITM/TUR181533/16)
  - Backend Development
  - Service Layer Implementation
  - Testing and Quality Assurance

- **Samuel Tekeste** (EITM/TUR181591/16)
  - Repository Layer Implementation
  - Database Integration
  - Documentation

- **Gebremariam**
  - UI Development
  - Feature Implementation
  - Testing and Bug Fixes

### Acknowledgments
- JavaFX team for the UI framework
- MySQL team for the database system
- All contributors who have helped improve the project
