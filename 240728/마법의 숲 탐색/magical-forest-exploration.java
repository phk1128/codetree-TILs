import java.util.*;
import java.io.*;


public class Main {

    private static int R;
    private static int C;
    private static int K;
    private static Golem[] golems;
    private static int[][] ds;
    private static int[][] golemVisited;
    private static boolean[][] exit;


    static class Golem {

        int idx;
        int c;
        int e;
        int[] elPos;

        public Golem(int idx, int c, int e) {
            this.idx = idx;
            this.c = c;
            this.e = e;
        }

        public void setE(int e) {
            this.e = e;
        }

        public void setElPos(int[] elPos) {
            this.elPos = elPos;
        }

    }

    public static void main(String[] args) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

        StringTokenizer st = new StringTokenizer(br.readLine());
        R = Integer.parseInt(st.nextToken());
        C = Integer.parseInt(st.nextToken());
        K = Integer.parseInt(st.nextToken());

        golems = new Golem[K + 1];
        ds = new int[][]{{-1, 0}, {0, 1}, {1, 0}, {0, -1}}; // 0,1,2,3 북동남서
        exit = new boolean[R + 2][C + 1];

        for (int i = 1; i <= K; i++) {
            st = new StringTokenizer(br.readLine());
            int c = Integer.parseInt(st.nextToken());
            int e = Integer.parseInt(st.nextToken());
            golems[i] = new Golem(i, c, e);
        }

        golemVisited = new int[R + 2][C + 1];
        List<Golem> movedGolems = new ArrayList<>();
        int answer = 0;
        for (int i = 1; i <= K; i++) {
            boolean flag = false;
            if (moveGolem(golems[i])) {
                movedGolems.add(golems[i]);
                flag = true;
            }

            if (flag && i < K) {
                continue;
            }

            for (Golem golem : movedGolems) {
                int[] elPos = golem.elPos;
                answer += (getScore(elPos[0], elPos[1]) - 1);
            }
            movedGolems.clear();
            golemVisited = new int[R + 2][C + 1];
        }

        bw.write(String.valueOf(answer));
        bw.flush();
        bw.close();

    }

    //정령을 이동시켜 최대 스코어를 가져온다.
    private static int getScore(int r, int c) {
        boolean[][] elVisited = new boolean[R + 2][C + 1];
        Queue<int[]> queue = new ArrayDeque<>();
        queue.offer(new int[]{r, c});
        int score = 0;
        elVisited[r][c] = true;

        while (!queue.isEmpty()) {
            int[] elPos = queue.poll();
            int cR = elPos[0];
            int cC = elPos[1];
            int idx = golemVisited[cR][cC];
            Golem golem = golems[idx];
            int eR = golem.elPos[0] + ds[golem.e][0];
            int eC = golem.elPos[1] + ds[golem.e][1];
            exit[eR][eC] = true;

            score = Math.max(cR, score);
            for (int[] d : ds) {
                int nR = cR + d[0];
                int nC = cC + d[1];

                if (!(nR >= 2 && nR <= R + 1 && nC >= 1 && nC <= C)) {
                    continue;
                }

                if (golemVisited[nR][nC] == 0 || elVisited[nR][nC]) {
                    continue;
                }

                //새로운 위치가 이전 골렘의idx와 다르고 현재위치가 출구위치가 아니라면 정령은 이동할 수 없다.
                if (golemVisited[nR][nC] != idx && !(cR == eR && cC == eC)) {
                    continue;
                }

                elVisited[nR][nC] = true;
                queue.offer(new int[]{nR, nC});
            }
        }
        
        return score;
    }

    //골렘을 움직인다
    private static boolean moveGolem(Golem golem) {

        int c = golem.c;
        int r = 1;
        int e = golem.e;

        while (true) {
            boolean isDown = false;
            boolean isLeft = false;
            boolean isRight = false;

            while (isMove(r + 1, c)) {
                r++;
                isDown = true;
            }

            if (r == R) {
                break;
            }

            if (isMove(r, c - 1) && isMove(r + 1, c - 1)) {
                c--;
                isLeft = true;
                e = (e - 1) % 4;
                if (e < 0) {
                    e += 4;
                }

            } else if (isMove(r, c + 1) && isMove(r + 1, c + 1)) {
                c++;
                isRight = true;
                e = (e + 1) % 4;
            }

            //내려갈 수도 없고, 왼쪽 오른쪽 둘다 회전이 안된다면 이동을 멈춰야한다.
            if (!isDown && !isLeft && !isRight) {
                break;
            }
        }

        //멈춘 영역이 숲 내부일 경우에만 idx를 방문처리하고, 골렘의 정보를 업데이트한다.
        if (r < 3) {
            return false;
        }
        golem.setE(e);
        golem.setElPos(new int[]{r, c});
        check(r, c, golem.idx);
        return true;

    }


    // 골렘idx으로 방문처리
    private static void check(int r, int c, int idx) {

        golemVisited[r][c] = idx;
        for (int[] d : ds) {
            golemVisited[r + d[0]][c + d[1]] = idx;
        }
    }


    // r,c를 중심으로 상하좌우 전부 확인
    private static boolean isMove(int r, int c) {
        if (golemVisited[r][c] != 0) {
            return false;
        }
        for (int[] d : ds) {
            int nR = r + d[0];
            int nC = c + d[1];
            if (!(nR >= 0 && nR <= R + 1 && nC >= 1 && nC <= C)) {
                return false;
            }
            if (golemVisited[nR][nC] != 0) {
                return false;
            }
        }
        return true;

    }
}

//맵의 행크기를 R + 2한다. 실제 숲의 영역은 2 ~ (R + 1)이 된다.
//숲의 영역의 R은 실제보다 +1이므로 계산한 스코어에서 -1을 해줘야한다.
//골렘은 회전 후 내려가는것 까지 가능해야 회전한다. 즉, 회전만하고 내려갈 수 없다면 회전하지 못한다.
//회전의 순서는 왼쪽이 먼저이고, 왼쪽으로 회전할 수 없다면 오른쪽으로 회전한다. 만약 왼쪽, 오른쪽 둘다 회전하지 못한다면 그 자리에 멈춘다.
//이동이 끝난 후에 현재 골렘의 위치가 숲 내부라면 골렘의 정보를 업데이트한다.
//정령이 이동할때 출구를 찾기 위해 방문처리는 골렘의 idx로 한다.
//새로운 위치가 이전 골렘의idx와 다르고 현재위치가 출구위치가 아니라면 정령은 이동할 수 없다.