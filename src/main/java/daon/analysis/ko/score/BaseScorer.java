package daon.analysis.ko.score;

import daon.analysis.ko.dict.config.Config;
import daon.analysis.ko.dict.connect.ConnectMatrix;
import daon.analysis.ko.model.Term;

import java.util.List;

/**
 * Created by mac on 2017. 1. 3..
 */
public class BaseScorer implements Scorer {

    private ConnectMatrix connectMatrix;

    private ScoreProperty scoreProperty;

    public BaseScorer(ConnectMatrix connectMatrix) {
        this.connectMatrix = connectMatrix;
    }

    public void setConnectMatrix(ConnectMatrix connectMatrix){
        this.connectMatrix = connectMatrix;
    }

    public void setScoreProperty(ScoreProperty scoreProperty) {
        this.scoreProperty = scoreProperty;
    }

    @Override
    public float score(Term prev, Term cur, Term next) {

        float score = 0;

        /*
        score += term.getKeyword().getTf();
        score += (term.getLength() / 2);
//		score += Math.log10(length); // slow..

        if(connectMatrix != null){
            //이전 term 과 인접 조건 체크
            if(prevTerm == null || Config.CharType.SPACE.equals(prevTerm.getCharType())){
                //root 조건
                if(connectMatrix.isValid("Root", tag)){
                    score += 0.1;
                }
            }else{
                //조합 조건 체크
                if(connectMatrix.isValid(prevTerm.getTag().name(), tag)){
                    score += 0.1;
                }
            }

            if(nextTerm != null ){
                boolean isValid = false;
                for(Term n : nextTerm){
                    //조합 조건 체크
                    if(connectMatrix.isValid(n.getTag().name(), tag)){
                        isValid = true;
                    }
                }

                if(isValid){
                    score += 0.1;
                }
            }
        }
        */

//        countTokens * profile.tokenCount +
//                countUnknowns * profile.unknown +
//                words * profile.wordCount +
//                getUnknownCoverage * profile.unknownCoverage +
//                getFreqScore * profile.freq +
//                countPos(Unknown) * profile.unknownPosCount +
//                isExactMatch * profile.exactMatch +
//                isAllNouns * profile.allNoun +
//                isPreferredPattern * profile.preferredPattern +
//                countPos(Determiner) * profile.determinerPosCount +
//                countPos(Exclamation) * profile.exclamationPosCount +
//                isInitialPostPosition * profile.initialPostPosition +
//                isNounHa * profile.haVerb +
//                hasSpaceOutOfGuide * profile.spaceGuidePenalty

        return score;
    }



}
