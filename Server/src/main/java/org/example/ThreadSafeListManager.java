package org.example;

import java.util.Set;

public class ThreadSafeListManager {
    private Set<Participant> synchronizedBlackListSet;
    private ThreadSafeLinkedList<Participant> lst;

    public ThreadSafeListManager(ThreadSafeLinkedList<Participant> lst, Set<Participant> synchronizedBlackListSet) {
        this.synchronizedBlackListSet = synchronizedBlackListSet;
        this.lst = lst;
    }

    public ThreadSafeLinkedList<Participant> getLst() {
        return lst;
    }

    public void processParticipant(Participant participant) {
        if(participant == null){
            System.out.println("RECEIVED NULL PARTCIPANT");
            return;
        }
//        System.out.println("PROCESSING PARTICIPANT");
        synchronized (lst) {
            int idx = lst.indexOf(participant);
            if (idx == -1) {
                if (participant.getScore() > -1 && !synchronizedBlackListSet.contains(participant)) {
                    lst.add(participant);
                } else {
                    synchronizedBlackListSet.add(participant);
                }
            } else {
                if (participant.getScore() == -1) {
                    lst.remove(participant);
                    synchronizedBlackListSet.add(participant);
                } else {
//                    System.out.println("UPDATING PARTICIPANT");
                    participant.setScore(participant.getScore() + lst.get(idx).getScore());
                    lst.set(idx, participant);
                }
            }
        }
    }
}
