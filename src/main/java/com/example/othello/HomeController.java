package com.example.othello;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    String beforeMessage = null;
    String afterMessage = null;

    // パラメータがセットされていない場合
    if (strList == null || strList.length() <= 0 || x == 0 || y == 0 || stone.length() <= 0) {

      // メッセージ
      beforeMessage = "さぁ、オセロを始めましょう。あなたが黒です。";

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

      if (x == 9 && y == 9) {
        // パスだったら
        beforeMessage = nowStone + "はパスしました";

        // 次の石にする
        nowStone = nowStone == StoneColor.BLACK ? StoneColor.WHITE : StoneColor.BLACK;
      } else {
        // パスじゃなかったら

        // 石を置き、挟んだ石をひっくり返す
        if (checkPiece(x, y, list, nowStone, true) > 0) {
          // メッセージ
          beforeMessage = nowStone + "が" + list.get(y).get(x).getAddress() + "に置かれました";

          // 石を置く
          list.get(y).get(x).setColor(nowStone);

          // 次の石
          if (nowStone == StoneColor.BLACK) {
            nowStone = StoneColor.WHITE;
          } else {
            nowStone = StoneColor.BLACK;
          }
        } else {
          // メッセージ
          beforeMessage = nowStone + "は" + Cell.columns[x] + y + "に置けません！";

        }

        // 文字列化
        strList = list2Str(list);
      }



    }


    // 石を置ける場所を探索する
    List<Cell> cells = new ArrayList<Cell>();
    int countBlank = 0, countBlack = 0, countWhite = 0;
    for (y = 1; y < ９; y++) {
      for (x = 1; x < ９; x++) {
        Cell cell = list.get(y).get(x);
        if (cell.getColor() == StoneColor.BLANK) {
          countBlank++;
          int countPlace = checkPiece(x, y, list, nowStone, false);
          if (countPlace > 0) {
            cells.add(cell);
            cell.setNext(nowStone);
            cell.setNextHref("?x=" + x + "&y=" + y + "&stone=" + nowStone.toString() + "&list=" + strList);
            cell.setCountPlace(countPlace);
          }
        } else if (cell.getColor() == StoneColor.BLACK) {
          countBlack++;
        } if (cell.getColor() == StoneColor.WHITE) {
          countWhite++;
        }

      }
    }

    if (countBlank == 0) {
      afterMessage = "ゲーム終了！ ";
      model.addAttribute("clear", true);
    } else {
      model.addAttribute("clear", false);

      if (nowStone == StoneColor.WHITE) {
        afterMessage = "白が検討中です・・・";
      } else {
        afterMessage = "あなた(黒)の番です";
      }

      // 置ける場所があったら
      if (cells.isEmpty() == false) {
        model.addAttribute("pass", false);
        if (nowStone == StoneColor.WHITE) {
          model.addAttribute("autoUrl", calcNextStone(cells).getNextHref());
        } else {
          model.addAttribute("autoUrl", calcNextStone2(cells).getNextHref());
        }
      } else {
        // パスする。
        model.addAttribute("pass", true);
        model.addAttribute("autoUrl", "?x=9&y=9&stone=" + nowStone.toString() + "&list=" + strList);
      }

    }

    model.addAttribute("beforeMessage", beforeMessage);
    model.addAttribute("afterMessage", afterMessage);
    model.addAttribute("statusMessage", "Black=" + countBlack + ", White=" + countWhite);

    model.addAttribute("nowStone", nowStone.toString());
    model.addAttribute("list", list);

    return "home";
  }

  /**
   * リストを文字列に変換する関数
   */
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

  /**
   * 文字列をリストに変換する関数
   */
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

  /**
   * 指定した場所に指定した色が置けるかどうかのチェック関数。flip=trueを指定すると置き換えも行う
   */
  private int checkPiece(int x, int y, List<List<Cell>> list, StoneColor nowTurn, boolean flip) {
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

    return ret;
  }

  /**
   * 置く場所を計算して返す関数
   */
  private Cell calcNextStone(List<Cell> cells) {
    if (cells == null || cells.size() <= 0) {
      return null;
    }
    int min = 99;
    Cell minCell = null;
    List<Cell> dengerCelsl = new ArrayList<Cell>();
    for (Cell cell : cells) {


      switch (cell.getAddress()) {
        // 四隅は最優先
        case "A1":  return cell;
        case "A8":  return cell;
        case "H1":  return cell;
        case "H8":  return cell;

        // 置きたくない場所
        case "A2":
        case "A7":
        case "B1":
        case "B2":
        case "B7":
        case "B8":
        case "G1":
        case "G2":
        case "G7":
        case "G8":
        case "H2":
        case "H7":
          dengerCelsl.add(cell);
          break;

        default:
          // ひっくり返せる数が少ないのを探す
          if (cell.getCountPlace() < min) {
            minCell =  cell;
            min = minCell.getCountPlace();
          }
          break;
      }


    }
    // 置きたくない場所以外の、ひっくり返せる数が最小の位置を返す
    if (minCell != null) {
      return minCell;
    }

    // 置きたくない場所しかない場合は、適当に戦闘のものを返す
    return dengerCelsl.get(0);
  }

  /**
   * 置く場所を計算して返す関数（適当バージョン）
   */
  private Cell calcNextStone2(List<Cell> cells) {
    if (cells == null || cells.size() <= 0) {
      return null;
    }
    Random random = new Random();
    int randomValue = random.nextInt(cells.size());
    return cells.get(randomValue);
  }
}
