package com.example.QuoraApp.controllers;

import com.example.QuoraApp.dto.QuestionRequestDTO;
import com.example.QuoraApp.dto.QuestionResponseDTO;
import com.example.QuoraApp.services.IQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final IQuestionService questionService;

    @PostMapping
    public Mono<QuestionResponseDTO> createQuestion(@RequestBody QuestionRequestDTO questionRequestDTO){
       return questionService.createQuestion(questionRequestDTO)
               .doOnSuccess(response -> System.out.println("Question created successfully: " + response))
               .doOnError(error -> System.out.println("Error creating question: " + error));
    }

    //offset based pagination method
    @GetMapping("/search")
    public Flux<QuestionResponseDTO> searchQuestions(@RequestParam String query, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "0") int size){
        return questionService.searchQuestions(query,page,size)
                .doOnComplete(() -> System.out.println("Questions fetched successfully: "))
                .doOnError(error -> System.out.println("Error fetching questions: " + error));
    }

    @GetMapping
    //cursor based pagination method
    public Flux<QuestionResponseDTO> getAllQuestions(@RequestParam(required = false) String cursor, @RequestParam(defaultValue = "10") int size){
        return questionService.getAllQuestions(cursor,size)
                .doOnComplete(() -> System.out.println("Questions fetched successfully: "))
                .doOnError(error -> System.out.println("Error fetching questions: " + error));
    }

    @GetMapping("/{id}")
    public Mono<QuestionResponseDTO> getQuestionById(@PathVariable String id){
        return questionService.getQuestionById(id)
                .doOnSuccess(response -> System.out.println("Question fetched successfully: " + response))
                .doOnError(error -> System.out.println("Error fetching question: " + error));
    }
}
