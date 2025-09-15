# Rook-ie: Chess Engine
A UCI-compatible chess engine written in Java, featuring piece-centric board representation, fast move generation, and neural network-based position evaluation. Supports modern gameplay features and advanced search control.

### Features
- __Piece-centric board representation__: Efficient array/list-based storage for fast move generation and easy piece manipulation.
- __Move generation in Java__: High-performance logic, with minimax search using alpha-beta pruning and dynamic depth selection based on game phase (opening, middlegame, endgame).
- __Neural network evaluation__: Position scoring is powered by a neural network. Weights and biases are imported from a Python-trained model, enabling a full forward pass during search for evaluation.
- __UCI protocol support__: Seamless integration with chess GUIs.
- __Customizable gameplay__: Easily adjust search depth and engine parameters to suit various playing strengths.

### Steps to run
- Clone the repository
  ```bash
  git clone https://github.com/VanshSukhija/rookie-chess-engine
  cd rookie-chess-engine
  ```
- Download the Lichess Evaluation Dataset from [here](https://database.lichess.org/lichess_db_eval.jsonl.zst).
- Extract the dataset and split the file into multiple files using:
  ```bash
  split -l NUMBER_OF_LINES input_file.txt output_prefix
  ```
- Run the jupiter notebook to train the neural network or load the network weights and biases.
- Install java dependencies
  ```bash
  mvn clean install
  ```
- Run `Application.java` to output a PGN (Portable Game Notation) in the terminal.
- Copy the PGN from the terminal and import the game [here](https://lichess.org/paste) to visualize.

### Architecture Overview
- __Board Representation__: Piece-centric list/array structure (not bitboards) for clarity and flexibility.
- __Move Generation__: Written in Java, the engine uses efficient loops and logic to create pseudo-legal/filtered legal moves.
- __Search Algorithm__: Minimax with alpha-beta pruning. Search depth is dynamically adjusted depending on the game phase (opening, middlegame, endgame).
- __Evaluation__: Upon reaching a leaf node, the Java engine imports neural network weights/biases (originally trained/exported from Python) and executes a forward pass to evaluate the position.
- __UCI Integration__: Responds to all essential UCI commands, enabling compatibility with external chess GUIs.
