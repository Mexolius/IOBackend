# IOBackend

Student grading app.

### Requires mongodb service at localhost:27017 (default for mongo)

Authorization is based on https://github.com/ximedes/ktor-authorization

## Endpoints

Short description of all REST API endpoints. Order is the same as in code. | resource | type | roles | description | object structure |
|-------------------------------------------|--------------|------------------------------------|-------------------------------------------------------------|---------------------------------------------------------|
| `/users`                                  | get | admin | returns all users | list of [user](/misc/exampleUser.json)                  | | `/user`
| post | admin | adds user | [user](/misc/exampleUser.json)                          | | `/users/{email}`                          | get | admin, matched by email | gets user
| [user](/misc/exampleUser.json)                          | | `/register`                               | post | no authentication | adds user | [user](/misc/exampleUser.json)
| | `/courses`                                | get | any authenticated | returns all courses | list of [course](/misc/exampleCourse.json)              | | `/course`
| post | admin, teacher | adds course | [course](/misc/exampleCourse.json)                      | | `/course/enroll/{course_id}`              | post | admin, teacher, student |
adds student's id to list | student's id (string)                                   | | `/courses/of-student/{user_id}`           | get | admin, teacher, matched by id | returns
courses of a student | list of:  [course](/misc/exampleCourse.json)            | | `/courses/of-teacher/{user_id}`           | get | admin, teacher, matched by id | returns courses
of a teacher | list of:  [course](/misc/exampleCourse.json)            | | `/courses/{user_id}/{course_id}`          | get | admin, teacher, matched by user_id | returns course
| [course](/misc/exampleCourse.json)                      | | `/grade/{course_id}`                      | post | admin, teacher | adds new grade to grade model
| [grade](/misc/exampleGrade.json)                        | | `/grades/{course_id}`                     | post | admin, teacher | adds new grades to grade model | list
of: [grade](/misc/exampleGrade.json)               | | `/grade/{course_id}/{grade_id}`           | post, delete | admin, teacher | updates or deletes grade
| [grade](/misc/exampleGrade.json)                        | | `/grade/{course_id}/{grade_id}/{user_id}` | post | admin, teacher | updates student's grade, creates a notification |
int | | `/export/course/{format}/{course_id}`     | get | admin, teacher | exports data to csv or xls format | binary content of a file | | `/notifications/user/{user_id}`
| get | admin, matched by id | returns notifications of a student, sorted oldest to newest | list of: [notification](/misc/exampleNotification.json) |
| `/notifications/user/{user_id}/clear`     | post | admin, matched by id | clears notifications of a student | --- |
