package ru.mdemidkin.intershop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.mdemidkin.intershop.server.api.DefaultApi;

@Controller
@RequestMapping
public class DefaultApiController implements DefaultApi {
}
