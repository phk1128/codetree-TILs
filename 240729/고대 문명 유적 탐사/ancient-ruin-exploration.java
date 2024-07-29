import java.util.*;
import java.io.*;


public class Main {

    private static final int R = 5;
    private static final int C = 5;
    private static boolean[][] visited;
    private static int[][] ds;
    private static PriorityQueue<int[]> tmpEmpties;
    private static Queue<Integer> nums;

    static class Result {

        int cR;
        int cC;
        int score;
        int turn;
        PriorityQueue<int[]> empties;

        public Result(int cR, int cC, int score, int turn, PriorityQueue<int[]> empties) {
            this.cR = cR;
            this.cC = cC;
            this.score = score;
            this.turn = turn;
            this.empties = empties;
        }

    }


    public static void main(String[] args) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

        StringTokenizer st = new StringTokenizer(br.readLine());
        int K = Integer.parseInt(st.nextToken());
        int M = Integer.parseInt(st.nextToken());

        visited = new boolean[6][6];
        ds = new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        nums = new ArrayDeque<>();
        int[][] mapView = new int[6][6];
        for (int r = 1; r <= R; r++) {
            st = new StringTokenizer(br.readLine());
            for (int c = 1; c <= C; c++) {
                mapView[r][c] = Integer.parseInt(st.nextToken());
            }
        }
        st = new StringTokenizer(br.readLine());
        for (int i = 0; i < M; i++) {
            nums.offer(Integer.parseInt(st.nextToken()));
        }

        StringBuilder sb = new StringBuilder();
        while (K-- > 0) {
            List<Result> results = new ArrayList<>();
            int total = 0;
            for (int r = 2; r <= R - 1; r++) {
                for (int c = 2; c <= C - 1; c++) {
                    int[][] copyOfMapView = copyOfArr(mapView);
                    for (int turn = 1; turn < 4; turn++) {
                        int[][] turnedMap = getTurnedMap(r, c, copyOfMapView);
                        int score = getScore(turnedMap);
                        results.add(new Result(r, c, score, turn, tmpEmpties));
                        copyOfMapView = turnedMap;
                    }
                }
            }
            Collections.sort(results, (r1, r2) -> {
                if (r1.score != r2.score) {
                    return r2.score - r1.score;
                }

                if (r1.turn != r2.turn) {
                    return r1.turn - r2.turn;
                }

                return r1.cC - r2.cC;
            });
            Result result = results.get(0);
            int turn = result.turn;
            total = result.score;
            int[][] copyOfMapView = copyOfArr(mapView);
            while (turn-- > 0) {
                int[][] turnedMap = getTurnedMap(result.cR, result.cC, copyOfMapView);
                copyOfMapView = turnedMap;
            }
            PriorityQueue<int[]> empties = result.empties;
            Queue<Integer> copyOfNums = new ArrayDeque<>(nums);
            addNum(copyOfMapView, copyOfNums, empties);

            while (true) {
                int score = getScore(copyOfMapView);
                if (score == 0) {
                    break;
                }
                total += score;
                addNum(copyOfMapView, copyOfNums, tmpEmpties);
            }
            mapView = copyOfMapView;
            if (total > 0) {
                sb.append(total);
                sb.append(" ");
            }

        }

        bw.write(sb.toString());
        bw.flush();
        bw.close();
    }


    private static void addNum(int[][] mapView, Queue<Integer> nums, PriorityQueue<int[]> empties) {
        if (nums.isEmpty() || empties.isEmpty()) {
            return;
        }

        while (!nums.isEmpty() && !empties.isEmpty()) {
            int num = nums.poll();
            int[] pos = empties.poll();
            int r = pos[0];
            int c = pos[1];
            mapView[r][c] = num;
        }
    }

    private static int getScore(int[][] mapView) {
        int score = 0;
        tmpEmpties = new PriorityQueue<>((e1, e2) -> {
            if (e1[1] != e2[1]) {
                return e1[1] - e2[1];
            }
            return e2[0] - e1[0];
        });
        visited = new boolean[R + 1][C + 1];
        for (int r = 1; r <= R; r++) {
            for (int c = 1; c <= C; c++) {
                if (visited[r][c] || mapView[r][c] == 0) {
                    continue;
                }
                List<int[]> piece = new ArrayList<>();
                int num = mapView[r][c];
                visited[r][c] = true;
                int count = getCount(r, c, num, piece, mapView);
                if (count >= 3) {
                    score += count;
                    tmpEmpties.addAll(piece);
                }
            }
        }

        return score;
    }

    private static int getCount(int r, int c, int num, List<int[]> piece, int[][] mapView) {

        visited[r][c] = true;
        int count = 1;
        piece.add(new int[]{r, c});

        for (int[] d : ds) {
            int nR = r + d[0];
            int nC = c + d[1];

            if (!isInBound(nR, nC)) {
                continue;
            }
            if (visited[nR][nC]) {
                continue;
            }
            if (num != mapView[nR][nC]) {
                continue;
            }
            count += getCount(nR, nC, num, piece, mapView);
        }
        return count;
    }

    private static boolean isInBound(int r, int c) {
        return r >= 1 && r <= R && c >= 1 && c <= C;
    }

    private static int[][] getTurnedMap(int cR, int cC, int[][] mapView) {

        int[][] turnedMap = copyOfArr(mapView);

        for (int r = cR - 1; r <= cR + 1; r++) {
            for (int c = cC - 1; c <= cC + 1; c++) {
                int nR = c + (cR - cC);
                int nC = (cR + cC) - r;
                turnedMap[nR][nC] = mapView[r][c];
            }
        }

        return turnedMap;

    }


    private static int[][] copyOfArr(int[][] arr) {
        int[][] copyOfArr = new int[R + 1][C + 1];
        for (int i = 0; i <= R; i++) {
            copyOfArr[i] = Arrays.copyOf(arr[i], C + 1);
        }
        return copyOfArr;
    }
}
// 흐름
// 결과 객체를 만든다. => 중심좌표, 턴횟수, 획득 유물 수
// 중심 (2,2) ~ (4,4) 까지 돌면서 각각 시계 방향으로 90,180,270 회전시켜본다.
// 각 회전 결과를 결과 객체로 만들고 list에 담는다.

//함수
//턴
//탐색(dfs)

//회전 규칙
//중심좌표 = (cR, cC)
//r,c -> c + (cR - cC), (cR + cC) - r