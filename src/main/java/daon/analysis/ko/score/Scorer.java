package daon.analysis.ko.score;


import daon.analysis.ko.model.Term;

public interface Scorer {

    public float score(Term prev, Term cur, Term next);
}
