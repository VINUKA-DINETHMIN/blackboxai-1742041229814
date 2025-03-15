# Skill Sharing Platform Backend

A Spring Boot backend application for a skill-sharing platform that enables users to share knowledge, create learning plans, and interact with other learners.

## Features

- User authentication with JWT and OAuth2 (Google)
- CRUD operations for skill-sharing posts
- Media upload support for posts
- Comment system
- Learning plan management
- Real-time notifications
- User following system
- Search functionality

## Technology Stack

- Java 17
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- H2 Database (can be replaced with PostgreSQL/MySQL)
- JWT Authentication
- OAuth2 Social Login
- Spring Cache
- Spring Scheduler

## Prerequisites

- JDK 17 or later
- Maven 3.6 or later
- Git

## Project Setup

1. Clone the repository:
```bash
git clone <repository-url>
cd backend
```

2. Configure application properties:
   - Update `src/main/resources/application.properties` with your database and OAuth2 credentials
   - Configure JWT secret and expiration time
   - Set up file upload directory

3. Build the project:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Documentation

### Authentication Endpoints

- `POST /api/auth/signup` - Register new user
- `POST /api/auth/login` - Login user
- `GET /oauth2/authorize/google` - Google OAuth2 login

### User Endpoints

- `GET /api/users/me` - Get current user
- `GET /api/users/{username}` - Get user profile
- `PUT /api/users/me` - Update current user
- `POST /api/users/{userId}/follow` - Follow user
- `POST /api/users/{userId}/unfollow` - Unfollow user

### Post Endpoints

- `POST /api/posts` - Create new post
- `GET /api/posts` - Get all posts
- `GET /api/posts/{postId}` - Get specific post
- `PUT /api/posts/{postId}` - Update post
- `DELETE /api/posts/{postId}` - Delete post
- `POST /api/posts/{postId}/like` - Like post
- `POST /api/posts/{postId}/unlike` - Unlike post

### Comment Endpoints

- `POST /api/posts/{postId}/comments` - Add comment
- `GET /api/posts/{postId}/comments` - Get post comments
- `PUT /api/comments/{commentId}` - Update comment
- `DELETE /api/comments/{commentId}` - Delete comment

### Learning Plan Endpoints

- `POST /api/learning-plans` - Create learning plan
- `GET /api/learning-plans/user/{userId}` - Get user's learning plans
- `PUT /api/learning-plans/{planId}` - Update learning plan
- `DELETE /api/learning-plans/{planId}` - Delete learning plan

### Notification Endpoints

- `GET /api/notifications` - Get user notifications
- `POST /api/notifications/{notificationId}/mark-read` - Mark notification as read
- `POST /api/notifications/mark-all-read` - Mark all notifications as read

## Security

The application uses JWT tokens for authentication. Include the JWT token in the Authorization header for protected endpoints:

```
Authorization: Bearer <your-token>
```

## File Upload

- Supported file types: Images (JPEG, PNG, GIF), Videos (MP4, QuickTime)
- Maximum file size: 10MB
- Files are stored in the configured upload directory

## Error Handling

The application provides detailed error responses in the following format:

```json
{
    "status": 400,
    "message": "Error message",
    "timestamp": "2023-01-01T12:00:00Z"
}
```

## Caching

The application implements caching for:
- User profiles
- Posts
- Comments
- Learning plans

## Scheduled Tasks

- Cleanup of old notifications
- Temporary file cleanup
- Cache maintenance

## Development

### Adding New Features

1. Create necessary model classes in `model` package
2. Create corresponding repository interfaces in `repository` package
3. Create DTOs in `dto` package
4. Implement service layer in `service` package
5. Create controller endpoints in `controller` package
6. Add security configurations if needed
7. Update documentation

### Testing

Run tests using:
```bash
mvn test
```

## Production Deployment

1. Update application-prod.properties with production configurations
2. Build production JAR:
```bash
mvn clean package -Pprod
```
3. Run the JAR:
```bash
java -jar target/skillsharing-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details
