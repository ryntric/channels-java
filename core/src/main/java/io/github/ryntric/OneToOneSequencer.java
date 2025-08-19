package io.github.ryntric;

/**
 * author: ryntric
 * date: 8/8/25
 * time: 11:00 PM
 **/

abstract class OneToOneSequencerLeftPaddings extends AbstractSequencer {
    protected byte
            p10, p11, p12, p13, p14, p15, p16, p17,
            p20, p21, p22, p23, p24, p25, p26, p27,
            p30, p31, p32, p33, p34, p35, p36, p37,
            p40, p41, p42, p43, p44, p45, p46, p47,
            p50, p51, p52, p53, p54, p55, p56, p57,
            p60, p61, p62, p63, p64, p65, p66, p67,
            p70, p71, p72, p73, p74, p75, p76, p77;

    public OneToOneSequencerLeftPaddings(PollerWaitPolicy pollerWaitPolicy, int bufferSize) {
        super(pollerWaitPolicy, bufferSize);
    }
}

abstract class OneToOneSequencerFields extends OneToOneSequencerLeftPaddings {
    long sequence = Sequence.INITIAL_VALUE;
    long cached = Sequence.INITIAL_VALUE;

    public OneToOneSequencerFields(PollerWaitPolicy pollerWaitPolicy, int bufferSize) {
        super(pollerWaitPolicy, bufferSize);
    }
}

abstract class OneToOneSequencerRightPaddings extends OneToOneSequencerFields {
    protected byte
            p10, p11, p12, p13, p14, p15, p16, p17,
            p20, p21, p22, p23, p24, p25, p26, p27,
            p30, p31, p32, p33, p34, p35, p36, p37,
            p40, p41, p42, p43, p44, p45, p46, p47,
            p50, p51, p52, p53, p54, p55, p56, p57,
            p60, p61, p62, p63, p64, p65, p66, p67,
            p70, p71, p72, p73, p74, p75, p76, p77;

    public OneToOneSequencerRightPaddings(PollerWaitPolicy pollerWaitPolicy, int bufferSize) {
        super(pollerWaitPolicy, bufferSize);
    }
}

public final class OneToOneSequencer extends OneToOneSequencerRightPaddings implements Sequencer {

    public OneToOneSequencer(PollerWaitPolicy pollerWaitPolicy, int bufferSize) {
        super(pollerWaitPolicy, bufferSize);
    }

    @Override
    public long next(int n) {
        long sequence = this.sequence;
        long next = sequence + n;
        long wrapPoint = next - bufferSize;
        long cached = this.cached;

        if (wrapPoint > cached || cached > sequence) {
            while (wrapPoint > (cached = gatingSequence.getAcquire())) {
//                LockSupport.parkNanos(1L);
            }
            this.cached = cached;
        }

        this.sequence = next;
        return next;
    }

    @Override
    public void publish(long value) {
        cursorSequence.setRelease(value);
        pollerWaitPolicy.signal();
    }

    @Override
    public void publish(long low, long high) {
        publish(high);
    }

    @Override
    public long getHighestPublishedSequence(long next, long available) {
        return available;
    }

}
