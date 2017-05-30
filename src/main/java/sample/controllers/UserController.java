package sample.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import sample.objects.ObjUser;
import sample.sql.UserService;

/**
 * Created by Denis on 17.02.2017.
 */

@RestController
@RequestMapping("/api/user/")
public class UserController {

    @Autowired
    UserService userService;

/*
    UserController(JdbcTemplate jdbcTemplate) {
        this.userService = new UserService(jdbcTemplate);
    }
*/

    //Создание нового пользователя
    @RequestMapping(path = "/{nickname}/create", method = RequestMethod.POST)
    public ResponseEntity<String> createUser(@RequestBody ObjUser body, @PathVariable(name = "nickname") String nickname) {
        System.out.println("Create USER with nickname " + nickname);

        try{
            return userService.create(body, nickname);
        } catch (DataAccessException e){
            return new ResponseEntity<>(userService.getUsers(body), HttpStatus.CONFLICT);
        }

    }

    //Получение информации о пользователе
    @RequestMapping(path = "/{nickname}/profile", method = RequestMethod.GET)
    public ResponseEntity<String> getUser(@PathVariable(name = "nickname") String nickname) {
        return (userService.get(nickname));
    }

    //Изменение данных о пользователе
    @RequestMapping(path = "/{nickname}/profile", method = RequestMethod.POST)
    public ResponseEntity<String> updateUser(@RequestBody ObjUser body,
                                             @PathVariable(name = "nickname") String nickname) {
        return (userService.update(body, nickname));
    }
}
