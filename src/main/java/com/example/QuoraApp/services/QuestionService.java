package com.example.QuoraApp.services;

import com.example.QuoraApp.adapter.QuestionAdapter;
import com.example.QuoraApp.dto.QuestionRequestDTO;
import com.example.QuoraApp.dto.QuestionResponseDTO;
import com.example.QuoraApp.events.ViewCountEvent;
import com.example.QuoraApp.models.Question;
import com.example.QuoraApp.producers.KafkaEventProducer;
import com.example.QuoraApp.repositories.QuestionRepository;
import com.example.QuoraApp.utils.CursorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QuestionService implements IQuestionService{

    private final QuestionRepository questionRepository;

    private final KafkaEventProducer kafkaEventProducer;

    @Override
    public Mono<QuestionResponseDTO> createQuestion(QuestionRequestDTO questionRequestDTO) {
        Question question = Question.builder()
                .title(questionRequestDTO.getTitle())
                .content(questionRequestDTO.getContent())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return questionRepository.save(question)
                .map(QuestionAdapter::toQuestionResponseDTO)
                .doOnSuccess(response -> System.out.println("Question created successfully: " + response))
                .doOnError(error -> System.out.println("Error creating question: " + error));
    }

    @Override
    public Flux<QuestionResponseDTO> searchQuestions(String searchTerm, int offset, int page){
        return questionRepository.findByTitleOrContentContainingIgnoreCase(searchTerm, PageRequest.of(offset,page))
                .map(QuestionAdapter::toQuestionResponseDTO)
                .doOnComplete(() -> System.out.println("Question searched successfully: "))
                .doOnError(error -> System.out.println("Error searching question: " + error));

    }

    @Override
    public Flux<QuestionResponseDTO> getAllQuestions(String cursor, int size){
        Pageable pageable = PageRequest.of(0,size);
        if(!CursorUtils.isValidCursor(cursor)){
            return questionRepository.findTop10ByOrderByCreatedAtAsc()
                    .take(size)
                    .map(QuestionAdapter::toQuestionResponseDTO)
                    .doOnComplete(() -> System.out.println("Questions fetched successfully: "))
                    .doOnError(error -> System.out.println("Error fetching question: " + error));
        }else{
            LocalDateTime cursorTimeStamp = CursorUtils.parseCursor(cursor);
            return questionRepository.findByCreatedAtGreaterThanOrderByCreatedAtAsc(cursorTimeStamp, pageable)
                    .map(QuestionAdapter::toQuestionResponseDTO)
                    .doOnComplete(() -> System.out.println("Questions fetched successfully: "))
                    .doOnError(error -> System.out.println("Error fetching question: " + error));
        }
    }

    @Override
    public Mono<QuestionResponseDTO> getQuestionById(String id){
        return questionRepository.findById(id)
                .map(QuestionAdapter::toQuestionResponseDTO)
                .doOnError(error -> System.out.println("Error fetching question: " + error))
                .doOnSuccess(response -> {
                    System.out.println("Questions fetched successfully: ");
                    ViewCountEvent viewCountEvent = new ViewCountEvent(id,"question", LocalDateTime.now());
                    kafkaEventProducer.publishViewCountEvent(viewCountEvent);
                });
    }
}
