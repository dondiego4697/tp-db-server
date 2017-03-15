package sample.controllers;

import org.springframework.beans.factory.annotation.Autowired;
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

    private final UserService userService;

    UserController(JdbcTemplate jdbcTemplate) {
        this.userService = new UserService(jdbcTemplate);
    }

    //Создание нового пользователя
    @RequestMapping(path = "/{nickname}/create", method = RequestMethod.POST)
    public void createUser(@RequestBody ObjUser body, @PathVariable(name = "nickname") String nickname) {
        userService.create(body, nickname);
    }

    //Получение информации о пользователе
    @RequestMapping(path = "/{nickname}/profile", method = RequestMethod.GET)
    public void getUser(@PathVariable(name = "nickname") String nickname) {

    }

    //Изменение данных о пользователе
    @RequestMapping(path = "/{nickname}/profile", method = RequestMethod.POST)
    public void updateUser(@PathVariable(name = "nickname") String nickname) {

    }
}
