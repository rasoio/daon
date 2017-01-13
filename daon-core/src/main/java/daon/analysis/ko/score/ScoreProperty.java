package daon.analysis.ko.score;

/**
 * Created by mac on 2017. 1. 3..
 */
public class ScoreProperty {

/*
    // Lower score is better
    case class TokenizerProfile(
    tokenCount: Float = 0.18f,
    unknown: Float = 0.3f,
    wordCount: Float = 0.3f,
    freq: Float = 0.2f,
    unknownCoverage: Float = 0.5f,
    exactMatch: Float = 0.5f,
    allNoun: Float = 0.1f,
    unknownPosCount: Float = 10.0f,
    determinerPosCount: Float = -0.01f,
    exclamationPosCount: Float = 0.01f,
    initialPostPosition: Float = 0.2f,
    haVerb: Float = 0.3f,
    preferredPattern: Float = 0.6f,
    preferredPatterns: Seq[Seq[Any]] = Seq(Seq(Noun, Josa), Seq(ProperNoun, Josa)),
    spaceGuide: Set[Int] = Set[Int](),
    spaceGuidePenalty: Float = 3.0f
*/

    //품사 연결 확율 가중치
    private float connectProb = 0.1f;

    //체언 여부
    private float allNoun = 0.3f;

    //음절 수 ?
    private float wordCount = 0.3f;

    //출현 빈도
    private float termFrequency = 0.5f;

    //선호 패턴
    private float preferredPattern = 0.6f;

//    private preferredPattern ?? 어떻게 정의할까..?

    public float getConnectProb() {
        return connectProb;
    }

    public void setConnectProb(float connectProb) {
        this.connectProb = connectProb;
    }
}
