package com.example.othello;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@EnableAutoConfiguration
public class HomeController {

  @GetMapping(path = "/")
  String home(HttpServletRequest request, Model model, @RequestParam(value = "list", required =  false, defaultValue = "") String strList, @RequestParam(value = "x", required =  false, defaultValue = "0")int x,@RequestParam(value = "y", required =  false, defaultValue = "0") int y, @RequestParam(value = "stone", required =  false, defaultValue = "") String stone) {

    List<List<Cell>> list;
    StoneColor nowStone = StoneColor.BLACK;
    StoneColor nextStone = StoneColor.WHITE;

    // パラメータがセットされていない場合
    if (strList == null || strList.length() <= 0 || x == 0 || y == 0 || stone.length() <= 0) {

      list = new ArrayList<List<Cell>>();
      for (int i = 0; i < 10; i++) {
        List<Cell> row = new ArrayList<Cell>();
        for (int j = 0; j < 10; j++) {
          row.add(new Cell(StoneColor.WALL, i, j));
        }
        list.add(row);
      }

      // ブランクで埋める
      for (int i = 1; i < ９; i++) {
        for (int j = 1; j < ９; j++) {
          list.get(i).get(j).setColor(StoneColor.BLANK);
        }
      }

      // 最初の石
      list.get(4).get(4).setColor(StoneColor.BLACK);
      list.get(5).get(5).setColor(StoneColor.BLACK);
      list.get(4).get(5).setColor(StoneColor.WHITE);
      list.get(5).get(4).setColor(StoneColor.WHITE);

      // 文字列化
      strList = list2Str(list);

    // パラメータがセットされている場合
    } else {
      // 文字列からリストに変換
      list = str2list(strList);

      nowStone = StoneColor.valueOf(stone);

      // 石を置き、挟んだ石をひっくり返す
      if (checkPiece(x, y, list, nowStone, true)) {

        // 石を置く
        list.get(y).get(x).setColor(nowStone);

        // 次の石
        if (nowStone == StoneColor.BLACK) {
          nowStone = StoneColor.WHITE;
        } else {
          nowStone = StoneColor.BLACK;
        }
      }

      // 文字列化
      strList = list2Str(list);


    }

    // 石を置ける場所を探索する
    for (y = 1; y < ９; y++) {
      for (x = 1; x < ９; x++) {
        if (list.get(y).get(x).getColor() == StoneColor.BLANK) {
          if (checkPiece(x, y, list, nowStone, false)) {
            list.get(y).get(x).setNext(nowStone);
            list.get(y).get(x).setNextHref("?x=" + x + "&y=" + y + "&stone=" + nowStone.toString() + "&list=" + strList);
          }
        }
      }
    }

    model.addAttribute("list", list);

    return "home";
  }
  private String list2Str(List<List<Cell>> list) {
    String ret = "";
    for (int i = 0; i < 10; i++) {
      String row = "";
      for (int j = 0; j < 10; j++) {
        if (j > 0) row += ",";
        if (list.get(i).get(j).getColor() == StoneColor.WHITE) {
          row += "1";
        } else if (list.get(i).get(j).getColor() == StoneColor.BLACK) {
          row += "2";
        } else if (list.get(i).get(j).getColor() == StoneColor.BLANK) {
          row += "3";
        } else {
          row += "0";
        }
      }
      row += ";";
      ret += row;
    }
    return ret;
  }
  private List<List<Cell>> str2list(String str) {
    List<List<Cell>> ret = new ArrayList<List<Cell>>();
    String[] rows = str.split(";");
    for (int i = 0; i < 10; i++) {
      List<Cell> l = new ArrayList<Cell>();
      String[] cols = rows[i].split(",");
      for(int j = 0; j < 10; j++) {
        switch (cols[j]) {
          case "0": l.add(new Cell(StoneColor.WALL, i, j)); break;
          case "1": l.add(new Cell(StoneColor.WHITE, i, j)); break;
          case "2": l.add(new Cell(StoneColor.BLACK, i, j)); break;
          case "3": l.add(new Cell(StoneColor.BLANK, i, j)); break;
        }
      }
      ret.add(l);
    }
    return ret;
  }

  private boolean checkPiece(int x, int y, List<List<Cell>> list, StoneColor nowTurn, boolean flip) {
    int ret = 0;
    StoneColor nextTern = nowTurn == StoneColor.BLACK ? StoneColor.WHITE : StoneColor.BLACK;

    // 8方向見ていく
    for (int dx = -1; dx <= 1; dx++) {
      for (int dy = -1; dy <= 1; dy++) {
        if (dx == 0 && dy == 0) continue;

        // 次のターンの石の色が続く限りすすめていく
        int nx = x + dx;
        int ny = y + dy;
        int n = 0;
        while (list.get(ny).get(nx).getColor() == nextTern) {
          n++;
          nx += dx;
          ny += dy;
        }
        // １つ以上進んだ & 進んだ先の色が現在のターンの色
        if (n > 0 && list.get(ny).get(nx).getColor() == nowTurn) {
          ret += n;
          if (flip) {
            nx = x + dx;
            ny = y + dy;
            n = 0;
            while (list.get(ny).get(nx).getColor() == nextTern) {
              list.get(ny).get(nx).setColor(nowTurn);
              n++;
              nx += dx;
              ny += dy;
            }
          }
        }
      }
    }

    return ret > 0;
  }
}
