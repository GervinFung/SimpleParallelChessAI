## NOTE: **Archived this repo, for latest code, check out the link below** 

https://github.com/GervinFung/LibGDX-Chess-Game


A simple 2 player chess AI that implemented minimax & alpha-beta pruning & move-ordering & pawn structure analysis to fully optimise the AI with a proper FEN file format

I screwed up my previous chess game with bad design and without planning ahead

I decided to make a chess game because my pc do not have one

NOTE: **Credit to Christos-Kouros for timer function**

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `res`: the folder to maintain resources like the images
- `lib`: the folder to maintain external libraries like Guava

## To play the Game
NOTE: **The image dragged is a bit low quality as image of piece is added to cursor**

No. | Steps
--- | ----
1.| Open Intellij Project.jar program in out/artifacts/Intellij_Project_jar
2.| User can choose to play as white/black
3.| User can choose white/black as AI
4.| Or, user can watch AI play against itself
5.| Once the game started, left press the piece you wish to move, drag and drop to the square you wish to place it
6.| OR, right click the piece you wish to move, and right click again to the square you wish to place it
7.| User can reset the timer if they wish to do so
8.| The piece will move as user drag

## Game Features

NOTE: **if AI move is undo, press its piece so AI can move again**

No. | Features
--- | ----
1.| Restart new game
2.| Save game/ Load saved game
3.| Exit game
4.| Highlight legal moves
5.| Show move history
6.| Choose AI level from 1 to 5
7.| Show captured piece
8.| Show AI thinking progress bar
9.| Undo previous move
10.| Multithread feature depends on the number of cores of a pc, the greater the better/faster AI can think
11.| Flip board
12.| Enabled/disable a timer **(Credit to Christos-Kouros)**

![Chess sample](chess.png)
[SimpleParallelChessAI.jar](out/artifacts/Intellij_Project_jar/Intellij%20Project.jar)
