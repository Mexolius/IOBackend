# IOBackend

Student grading app.

### Requires mongodb service at localhost:27017 (default for mongo)

Authorization is based on https://github.com/ximedes/ktor-authorization

## Endpoints

Short description of all REST API endpoints. Order is the same as in code.
| resource                                     | type | roles                              | description                           | object structure                                     |
|----------------------------------------------|------|------------------------------------|---------------------------------------|------------------------------------------------------|
| `/api/users`                                 | get  | admin                              | returns all users                     | ---                                                  |
| `/api/user`                                  | post | admin                              | adds user                             | [user](/misc/exampleUser.json)                       |
| `/api/users/{"email"}`                       | get  | admin, matched by email            | gets user                             | ---                                                  |
| `/api/register`                              | post | no authentication                  | adds user                             | [user](/misc/exampleUser.json)                       |
| `/api/courses`                               | get  | any authenticated                  | returns all courses                   | ---                                                  |
| `/api/course`                                | post | admin, teacher                     | adds course                           | [course](/misc/exampleCourse.json)                   |
| `/api/course/grade/{course_id}`              | post | admin, teacher                     | updates grade model                   | [grade](/misc/exampleGrade.json)                     |
| `/api/course/grade/thresholds/{course_id}`   | post | admin, teacher                     | updates grade thresholds              | [grade thresholds](/misc/exampleGradeThresholds.json)|
| `/api/course/grade/{course_id}/{student_id}` | post | admin, teacher                     | updates student's grade               | [student's grade](/misc/exampleGradeStudent.json)    |
| `/api/courses/of-student/{id}`               | get  | admin, teacher, matched by id      | returns courses of a student          | ---                                                  |
| `/api/courses/of-teacher/{id}`               | get  | admin, teacher, matched by id      | returns courses of a teacher          | ---                                                  |
| `/api/courses/{user_id}/{course_id}`         | get  | admin, teacher, matched by user_id | returns course if student is enrolled | ---                                                  |
