package com.example.othello;

import lombok.Data;

@Data
public class Cell {
  private StoneColor color;
  private StoneColor next;
  private String nextHref;
  private int row;
  private int col;
  private String address;
  private int countPlace;

  public static final String[] columns = {"", "A", "B", "C", "D", "E", "F", "G", "H", ""};


  public Cell(StoneColor color, int row, int col) {
    this.color = color;
    this.row = row;
    this.col = col;
    this.next = StoneColor.BLANK;

    if (row == 0 || row == ９) {
      this.address = columns[col];
    } else {
      this.address = columns[col] + row;
    }
  }

  public String getColorKanji() {
    if (color == StoneColor.BLACK) {
      return "黒";
    } else if (color == StoneColor.WHITE) {
      return "白";
    }
    return "";
  }


}
