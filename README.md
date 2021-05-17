# IOBackend

Student grading app.

### Requires mongodb service at localhost:27017 (default for mongo)

Authorization is based on https://github.com/ximedes/ktor-authorization

## Endpoints

Short description of all REST API endpoints. Order is the same as in code.
| resource                                         | type         | roles                              | description                           | object structure                         |
|--------------------------------------------------|--------------|------------------------------------|---------------------------------------|------------------------------------------|
| `/api/users`                                     | get          | admin                              | returns all users                     | ---                                      |
| `/api/user`                                      | post         | admin                              | adds user                             | [user](/misc/exampleUser.json)           |
| `/api/users/{email}`                             | get          | admin, matched by email            | gets user                             | ---                                      |
| `/api/register`                                  | post         | no authentication                  | adds user                             | [user](/misc/exampleUser.json)           |
| `/api/courses`                                   | get          | any authenticated                  | returns all courses                   | ---                                      |
| `/api/course`                                    | post         | admin, teacher                     | adds course                           | [course](/misc/exampleCourse.json)       |
| `/api/course/enroll/{course_id}`                 | post         | admin, teacher, student            | adds student's id to list             | student's id (string)                    |
| `/api/courses/of-student/{user_id}`              | get          | admin, teacher, matched by id      | returns courses of a student          | ---                                      |
| `/api/courses/of-teacher/{user_id}`              | get          | admin, teacher, matched by id      | returns courses of a teacher          | ---                                      |
| `/api/courses/{user_id}/{course_id}`             | get          | admin, teacher, matched by user_id | returns course                        | ---                                      |
| `/api/grade/{course_id}`                         | post         | admin, teacher                     | adds new grade to grade model         | [grade](/misc/exampleGrade.json)         |
| `/api/grades/{course_id}`                        | post         | admin, teacher                     | adds new grades to grade model        | list of: [grade](/misc/exampleGrade.json)|
| `/api/grade/{course_id}/{grade_id}`              | post, delete | admin, teacher                     | updates or deletes grade              | [grade](/misc/exampleGrade.json)         |
|`/api/grade/many/{course_id}/{grade_id}`          |post          |admin, teacher                     |sets mutiple students' grades           |map from [user_id](/misc/exampleUser.json) to int |
| `/api/grade/{course_id}/{grade_id}/{user_id}`    | post         | admin, teacher                     | updates student's grade               | int                                      |
| `/api/export/course/{format}/{course_id}`        | get          | admin, teacher                     | exports data to csv or xls format     | ---                                      |
