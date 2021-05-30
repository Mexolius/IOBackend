# IOBackend

Student grading app.

### Requires mongodb service at localhost:27017 (default for mongo)

Authorization is based on https://github.com/ximedes/ktor-authorization

## Endpoints

Short description of all REST API endpoints. Order is the same as in code.

| resource                                                      | verb   | roles                                  | description                                                 | object structure                                                         |
|---------------------------------------------------------------|--------|----------------------------------------|-------------------------------------------------------------|--------------------------------------------------------------------------|
| `/users`                                                      | GET    | admin                                  | returns all users                                           | list of [user](/misc/exampleUser.json)                                   |
| `/user`                                                       | POST   | admin                                  | adds user                                                   | [user](/misc/exampleUser.json)                                           |
| `/users/{email}`                                              | GET    | admin, matched by email                | gets user                                                   | [user](/misc/exampleUser.json)                                           |
| `/notifications/user/{user_id}`                               | GET    | admin, matched by id                   | returns notifications of a student, sorted oldest to newest | list of: [notification](/misc/exampleNotification.json)                  |
| `/notifications/user/{user_id}`                               | DELETE | admin, matched by id                   | clears notifications of a student                           | ---                                                                      |
| `/notifications/user/{user_id}/{course_id}`                   | DELETE | admin, matched by id                   | clears notifications from given course of a student         | ---                                                                      |
| `/register`                                                   | POST   | no authentication                      | adds user                                                   | [user](/misc/exampleUser.json)                                           |
| `/courses`                                                    | GET    | any authenticated                      | returns all courses                                         | list of [course](/misc/exampleCourseFromFront.json)                      |
| `/course`                                                     | POST   | admin, teacher                         | adds course                                                 | [course](/misc/exampleCourseFromFront.json)                              |
| `/course/enroll-by-email/{course_id}`                         | POST   | admin, teacher                         | adds user's id to appropriate list                          | user's email (string)                                                    |
| `/course/enroll-by-id/{course_id}`                            | POST   | admin, teacher, student                | adds student's id to list of students                       | student's id (string)                                                    |
| `/courses/of-student/{user_id}`                               | GET    | admin, teacher, matched by id          | returns courses of a student                                | list of:  [course](/misc/exampleCourseFromFront.json)                    |
| `/courses/of-teacher/{user_id}`                               | GET    | admin, teacher, matched by id          | returns courses of a teacher                                | list of:  [course](/misc/exampleCourseFromFront.json)                    |
| `/courses/{user_id}/{course_id}`                              | GET    | admin, teacher, matched by user_id     | returns course                                              | [course](/misc/exampleCourseFromFront.json)                              |
| `/grade/{course_id}`                                          | POST   | admin, teacher                         | adds new grade to grade model                               | [grade](/misc/exampleGrade.json)                                         |
| `/grades/{course_id}`                                         | POST   | admin, teacher                         | adds new grades to grade model                              | list of: [grade](/misc/exampleGrade.json)                                |
| `/grade/{course_id}/{grade_id}`                               | POST   | admin, teacher                         | updates grade                                               | [grade](/misc/exampleGrade.json)                                         |
| `/grade/{course_id}/{grade_id}`                               | DELETE | admin, teacher                         | deletes grade                                               | [grade](/misc/exampleGrade.json)                                         |
| `/grade/many/{course_id}/{grade_id}`                          | POST   | admin, teacher                         | updates multiple students' grades                           | map from [user_id](/misc/exampleUser.json) to int                        |
| `/grade/{course_id}/{grade_id}/{user_id}`                     | POST   | admin, teacher                         | updates student's grade, creates a notification             | int                                                                      |
| `/export/course/{format}/{course_id}`                         | GET    | admin, teacher                         | exports data to csv or xls format                           | binary content of a file                                                 |
| `/histogram/grades/{course_id}/{user_id}`                     | GET    | admin, teacher, student, matched by id | returns sorted list of points                               | map from gradeID to [gradeHistogram](/misc/exampleGradeHistogram.json)   |
| `/histogram/buckets/{buckets}/{course_id}/{user_id}`          | GET    | admin, teacher, student, matched by id | returns list of buckets of approx. length of {buckets}      | map from gradeID to [bucketHistogram](/misc/exampleBucketHistogram.json) |
| `/histogram/bucketsWithEmpty/{buckets}/{course_id}/{user_id}` | GET    | admin, teacher, student, matched by id | same as above, but includes empty buckets                   | map from gradeID to [bucketHistogram](/misc/exampleBucketHistogram.json) |

