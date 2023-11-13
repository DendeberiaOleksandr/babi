package org.babi.backend.question.dao;

import lombok.Getter;
import lombok.Setter;
import org.babi.backend.common.dao.AbstractCriteria;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
public class QuestionCriteria extends AbstractCriteria {

    Long questionId;

    public QuestionCriteria(Long page, Long size, Long questionId) {
        super(page, size);
        this.questionId = questionId;
    }

    public QuestionCriteria(Long questionId) {
        this.questionId = questionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        QuestionCriteria that = (QuestionCriteria) o;
        return Objects.equals(questionId, that.questionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), questionId);
    }

    @Override
    public Map<String, Object> mapCriteriaToQueryArgs(StringBuilder sql) {
        Map<String, Object> args = new HashMap<>();
        boolean whereClauseAdded = false;

        if (questionId != null) {
            appendWhereOrAndClause(whereClauseAdded, sql);
            whereClauseAdded = true;
            sql.append("q.id = :id");
            args.put("id", questionId);
        }

        mapOffsetLimitCriteriaToQueryArgs(args, sql);

        return args;
    }
}
