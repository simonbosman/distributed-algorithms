package week1;

import java.util.Random;

public enum Item {

    ROCK, PAPER, SCISSORS;

    public static Item random() {
        return values()[new Random().nextInt(3)];
    }

    public boolean beats(Item i) {
        return (this == ROCK && i == SCISSORS) || (this == PAPER && i == ROCK) || (this == SCISSORS && i == PAPER);
    }

    // Returns the item that beats the given item
    public Item getBeatingItem() {
        if (this == ROCK) {
            return PAPER;
        } else if (this == PAPER) {
            return SCISSORS;
        } else {
            return ROCK;
        }
    }
}
