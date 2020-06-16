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


  public Cell(StoneColor color, int row, int col) {
    this.color = color;
    this.row = row;
    this.col = col;
    this.next = StoneColor.BLANK;

    String[] columns = {"", "A", "B", "C", "D", "E", "F", "G", "H", ""};
    if (row == 0 || row == ９) {
      this.address = columns[col];
    } else {
      this.address = columns[col] + row;
    }
  }

}
