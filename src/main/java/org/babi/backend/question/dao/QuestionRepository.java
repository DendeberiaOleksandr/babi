package org.babi.backend.question.dao;

import org.babi.backend.common.dao.ReactivePageableRepository;
import org.babi.backend.common.dao.ReactiveRepository;
import org.babi.backend.question.domain.Question;

public interface QuestionRepository extends ReactiveRepository<Long, Question>, ReactivePageableRepository<Long, Question> {

}
