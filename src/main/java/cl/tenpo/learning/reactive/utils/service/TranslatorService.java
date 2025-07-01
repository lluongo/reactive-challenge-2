package cl.tenpo.learning.reactive.utils.service;

import org.springframework.stereotype.Service;

@Service
public class TranslatorService {

    public String translate(String text) {
        return text.concat("-inho");
    }

}
