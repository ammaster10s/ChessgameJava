package Code;




public class Piece {

	protected boolean color;
	int Visibility;
	static String Pieces;

	static boolean hascastleb = true;
	static boolean hascastlew = true;

	static boolean enpassant = false;
	static int[] enpassantpossible = new int[1];
	static boolean incheck = false;

	private static int[] scanXKQ = { -1, 0, 1, 1, 1, 0, -1, -1 };
	private static int[] scanYKQ = { 1, 1, 1, 0, -1, -1, -1, 0 };

	private static int[] scanXpawn = { -1, 0, 1 };
	private static int[] scanYpawn = { 1, 1, 1 };

	private static int[] scanXbis = { -1, 1, 1, -1 };
	private static int[] scanYbis = { 1, 1, -1, -1 };

	private static int[] scanXrook = { 0, 1, 0, -1 };
	private static int[] scanYrook = { 1, 0, -1, 0 };

	private static int[] scanXknight = { -2, -1, 1, 2, 2, 1, -1, -2 };
	private static int[] scanYknight = { 1, 2, 2, 1, -1, -2, -2, -1 };
	static String[][] posiblemove = new String[8][8];

	public Piece() {
	}

	public Piece(int x, int y, String type, boolean Color1) {
		if (type == "pawn") {
			Pawn(x, y, Color1);
		}
		if (type == "king") {
			King(x, y, Color1);
		}
		if (type == "queen") {
			Queen(x, y, Color1);
		}
		if (type == "bishop") {
			Bishop(x, y, Color1);
		}
		if (type == "rook") {
			Rook(x, y, Color1);
		}
		if (type == "night") {
			Knight(x, y, Color1);
		}
	}

	public void Pawn(int x, int y, boolean Color1) {
		if (Color1 == true) {
			Chessboard.board[x][y] = "Wpawn";
		} else {
			Chessboard.board[x][y] = "Bpawn";
		}

	}

	public void King(int x, int y, boolean Color1) {

		if (Color1 == true) {
			Chessboard.board[x][y] = "Wking";
		} else {
			Chessboard.board[x][y] = "Bking";
		}

	}

	public void Knight(int x, int y, boolean Color1) {

		if (Color1 == true) {
			Chessboard.board[x][y] = "Wnight";
		} else {
			Chessboard.board[x][y] = "Bnight";
		}

	}

	public void Queen(int x, int y, boolean Color1) {
		if (Color1 == true) {
			Chessboard.board[x][y] = "Wqueen";
		} else {
			Chessboard.board[x][y] = "Bqueen";
		}
	}

	public void Bishop(int x, int y, boolean Color1) {
		if (Color1 == true) {
			Chessboard.board[x][y] = "Wbishop";
		} else {
			Chessboard.board[x][y] = "Bbishop";
		}
	}

	public void Rook(int x, int y, boolean Color1) {
		if (Color1 == true) {
			Chessboard.board[x][y] = "Wrook";
		} else {
			Chessboard.board[x][y] = "Brook";
		}
	}

	public String checklayoutboard(int y, int x) {

		if (Chessboard.board[y][x] != null) {
			Pieces = Chessboard.board[y][x];

			if (Pieces.charAt(1) == 'p' || Pieces.charAt(1) == 'k') {
				this.Visibility = 1;
				if (Pieces.charAt(0) == 'W') {
					this.color = true;
				} else {
					this.color = false;
				}
			}
			if (Pieces.charAt(1) == 'q' || Pieces.charAt(1) == 'b' || Pieces.charAt(1) == 'r') {
				this.Visibility = 8;
				if (Pieces.charAt(0) == 'W') {
					this.color = true;
				} else {
					this.color = false;
				}
			}
			if (Pieces.charAt(1) == 'n') {
				if (Pieces.charAt(0) == 'W') {
					this.color = true;
				} else {
					this.color = false;
				}
			}
		}

		else {
			return (Pieces = null);
		}

		return Pieces;

	}

	public void posiblemovecal(int y, int x) {

		if (this.color == false && MouseMotion.turntomove == false) {

			if (Pieces.charAt(1) == 'k' || Pieces.charAt(1) == 'q') { // King / Queen 
				for (int i = 0; i < scanXKQ.length; i++) {
					for (int j = 1; j <= Visibility; j += 1) {
						try {

							if (Chessboard.board[y + (scanYKQ[i] * j)][x + (scanXKQ[i] * j)] == null) {
								posiblemove[y + (scanYKQ[i] * j)][x + (scanXKQ[i] * j)] = "Posible";
							}

							else if (Chessboard.board[y + (scanYKQ[i] * j)][x + (scanXKQ[i] * j)].charAt(0) == 'W') {
								posiblemove[y + (scanYKQ[i] * j)][x + (scanXKQ[i] * j)] = "Posible";
								break;
							} else if (Pieces.charAt(1) == 'k' || Pieces.charAt(1) == 'q') {

								if (Chessboard.board[y + (scanYKQ[i] * j)][x + (scanXKQ[i] * j)].charAt(0) == 'B') {

									break;
								}
							}

						} catch (ArrayIndexOutOfBoundsException e) {
							continue;
						}

					}
				}
			} else if (Pieces.charAt(1) == 'p') { // Pawn
				for (int i = 0; i < scanXpawn.length; i++) {
					for (int j = 1; j <= Visibility; j += 1) {
						try {

							if (Chessboard.board[y + (scanYpawn[i] * j)][x + (scanXpawn[i] * j)] == null) {
								if (scanXpawn[i] == 0) {
									if (y == 1) {
										posiblemove[y + (scanYpawn[i] * 2 * j)][x + (scanXpawn[i] * j)] = "Posible";
										posiblemove[y + (scanYpawn[i] * j)][x + (scanXpawn[i] * j)] = "Posible";
										enpassantpossible[1]= x;
										enpassant=true;
									}

									posiblemove[y + (scanYpawn[i] * j)][x + (scanXpawn[i] * j)] = "Posible";
								}
							} else if (Chessboard.board[y + (scanYpawn[i] * j)][x + (scanXpawn[i] * j)]
									.charAt(0) == 'W') {
								if (scanXpawn[i] != 0) {
									posiblemove[y + (scanYpawn[i] * j)][x + (scanXpawn[i] * j)] = "Posible";
									break;
								}
							}

						} catch (ArrayIndexOutOfBoundsException e) {
							continue;
						}

					}
				}
			} else if (Pieces.charAt(1) == 'b') {
				for (int i = 0; i < scanXbis.length; i++) {
					for (int j = 1; j <= Visibility; j += 1) {
						try {

							if (Chessboard.board[y + (scanYbis[i] * j)][x + (scanXbis[i] * j)] == null) {
								posiblemove[y + (scanYbis[i] * j)][x + (scanXbis[i] * j)] = "Posible";
							}

							else if (Chessboard.board[y + (scanYbis[i] * j)][x + (scanXbis[i] * j)].charAt(0) == 'W') {

								posiblemove[y + (scanYbis[i] * j)][x + (scanXbis[i] * j)] = "Posible";
								break;

							} else if (Chessboard.board[y + (scanYbis[i] * j)][x + (scanXbis[i] * j)]
									.charAt(0) == 'B') {

								break;
							}
						} catch (ArrayIndexOutOfBoundsException e) {
							continue;
						}

					}
				}
			} else if (Pieces.charAt(1) == 'r') {
				for (int i = 0; i < scanXrook.length; i++) {
					for (int j = 1; j <= Visibility; j += 1) {
						try {

							if (Chessboard.board[y + (scanYrook[i] * j)][x + (scanXrook[i] * j)] == null) {
								posiblemove[y + (scanYrook[i] * j)][x + (scanXrook[i] * j)] = "Posible";
							}

							else if (Chessboard.board[y + (scanYrook[i] * j)][x + (scanXrook[i] * j)]
									.charAt(0) == 'W') {

								posiblemove[y + (scanYrook[i] * j)][x + (scanXrook[i] * j)] = "Posible";
								break;

							} else if (Chessboard.board[y + (scanYrook[i] * j)][x + (scanXrook[i] * j)]
									.charAt(0) == 'B') {

								break;
							}
						} catch (ArrayIndexOutOfBoundsException e) {
							continue;
						}

					}
				}
			}

			else if (Pieces.charAt(1) == 'n') {
				for (int i = 0; i < scanXknight.length; i++) {

					try {

						if (Chessboard.board[y + (scanYknight[i])][x + (scanXknight[i])] == null) {
							posiblemove[y + (scanYknight[i])][x + (scanXknight[i])] = "Posible";
						}

						else if (Chessboard.board[y + (scanYknight[i])][x + (scanXknight[i])]
								.charAt(0) == 'W') {

							posiblemove[y + (scanYknight[i])][x + (scanXknight[i])] = "Posible";
							continue;

						} else if (Chessboard.board[y + (scanYknight[i])][x + (scanXknight[i])]
								.charAt(0) == 'B') {

							continue;
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						continue;
					}

				}

			}

		}

		if (this.color == true && MouseMotion.turntomove == true) {

			if (Pieces.charAt(1) == 'k' || Pieces.charAt(1) == 'q') { // King / Queen
				for (int i = 0; i < scanXKQ.length; i++) {
					for (int j = 1; j <= Visibility; j += 1) {
						try {

							if (Chessboard.board[y - (scanYKQ[i] * j)][x + (scanXKQ[i] * j)] == null) {
								posiblemove[y - (scanYKQ[i] * j)][x + (scanXKQ[i] * j)] = "Posible";
							}

							else if (Chessboard.board[y - (scanYKQ[i] * j)][x + (scanXKQ[i] * j)].charAt(0) == 'B') {
								posiblemove[y - (scanYKQ[i] * j)][x + (scanXKQ[i] * j)] = "Posible";
								break;

							} else if (Pieces.charAt(1) == 'k' || Pieces.charAt(1) == 'q') {

								if (Chessboard.board[y - (scanYKQ[i] * j)][x + (scanXKQ[i] * j)].charAt(0) == 'W') {

									break;
								}
							}
						} catch (ArrayIndexOutOfBoundsException e) {
							continue;
						}

					}
				}
			} else if (Pieces.charAt(1) == 'p') { // Pawn
				for (int i = 0; i < scanXpawn.length; i++) {
					for (int j = 1; j <= Visibility; j += 1) {
						try {

							if (Chessboard.board[y - (scanYpawn[i] * j)][x + (scanXpawn[i] * j)] == null) {
								if (scanXpawn[i] == 0) {
									if (y == 6) {
										posiblemove[y - (scanYpawn[i] * 2 * j)][x + (scanXpawn[i] * j)] = "Posible";
										posiblemove[y - (scanYpawn[i] * j)][x + (scanXpawn[i] * j)] = "Posible";
										enpassantpossible[1] =x;
										enpassant= true;
									}
									posiblemove[y - (scanYpawn[i] * j)][x + (scanXpawn[i] * j)] = "Posible";
									
								}
							} else if (Chessboard.board[y - (scanYpawn[i] * j)][x + (scanXpawn[i] * j)]
									.charAt(0) == 'B') {
								if (scanXpawn[i] != 0) {
									posiblemove[y - (scanYpawn[i] * j)][x + (scanXpawn[i] * j)] = "Posible";
									break;
								}
							}
							checkenpassant(y, x);
						} catch (ArrayIndexOutOfBoundsException e) {
							continue;
						}

					}
				}
			}

			else if (Pieces.charAt(1) == 'b') {
				for (int i = 0; i < scanXbis.length; i++) {
					for (int j = 1; j <= Visibility; j += 1) {
						try {

							if (Chessboard.board[y - (scanYbis[i] * j)][x + (scanXbis[i] * j)] == null) {
								posiblemove[y - (scanYbis[i] * j)][x + (scanXbis[i] * j)] = "Posible";
							}

							else if (Chessboard.board[y - (scanYbis[i] * j)][x + (scanXbis[i] * j)].charAt(0) == 'B') {

								posiblemove[y - (scanYbis[i] * j)][x + (scanXbis[i] * j)] = "Posible";
								break;

							} else if (Chessboard.board[y - (scanYbis[i] * j)][x + (scanXbis[i] * j)]
									.charAt(0) == 'W') {

								break;
							}
						} catch (ArrayIndexOutOfBoundsException e) {
							continue;
						}

					}
				}
			} else if (Pieces.charAt(1) == 'r') {
				for (int i = 0; i < scanXrook.length; i++) {
					for (int j = 1; j <= Visibility; j += 1) {
						try {

							if (Chessboard.board[y - (scanYrook[i] * j)][x + (scanXrook[i] * j)] == null) {
								posiblemove[y - (scanYrook[i] * j)][x + (scanXrook[i] * j)] = "Posible";
							}

							else if (Chessboard.board[y - (scanYrook[i] * j)][x + (scanXrook[i] * j)]
									.charAt(0) == 'B') {

								posiblemove[y - (scanYrook[i] * j)][x + (scanXrook[i] * j)] = "Posible";
								break;

							} else if (Chessboard.board[y - (scanYrook[i] * j)][x + (scanXrook[i] * j)]
									.charAt(0) == 'W') {

								break;
							}
						} catch (ArrayIndexOutOfBoundsException e) {
							continue;
						}

					}
				}
			} else if (Pieces.charAt(1) == 'n') {
				System.out.println("pass");
				for (int i = 0; i < scanXknight.length; i++) {

					try {

						if (Chessboard.board[y + (scanYknight[i])][x + (scanXknight[i])] == null) {
							posiblemove[y + (scanYknight[i])][x + (scanXknight[i])] = "Posible";
						}

						else if (Chessboard.board[y + (scanYknight[i])][x + (scanXknight[i])]
								.charAt(0) == 'B') {

							posiblemove[y + (scanYknight[i])][x + (scanXknight[i])] = "Posible";
							continue;

						} else if (Chessboard.board[y + (scanYknight[i])][x + (scanXknight[i])]
								.charAt(0) == 'W') {

							continue;
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						continue;
					}

				}

			}
		}
	}

	public void checkking(int y, int x) {
		posiblemove[y][x] = "Check";
	}

	public void checkenpassant(int y, int x){
		if (Piece.Pieces.charAt(0) == 'B' && y == 4 && enpassant == true){
			posiblemove[y+1][enpassantpossible[1]] = "Posible";
			enpassant= false;
			System.out.println("Benpassant");
		}
		if (Piece.Pieces.charAt(0) == 'W' && y == 3 && enpassant == true){
			posiblemove[y-1][enpassantpossible[1]] = "Posible";
			enpassant= false;
			System.out.println("Wenpassant");
		}
	}

	public static void promotion(int y, int x, boolean color, int yprev, int xprev) {
		if (color == true && y == 0) {

			Chessboard.board[yprev][xprev] = null;
			Chessboard.board[y][x] = "Wqueen";
		} else if (color == false && y == 7) {
			Chessboard.board[yprev][xprev] = null;
			Chessboard.board[y][x] = "Bqueen";
		}
	}

}