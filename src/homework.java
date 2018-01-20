import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
//import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class homework {

	private static int MAX_DEPTH = 2;
	static int n,p;
	static int arr[][] = new int[26][26];
	static int scores[];
	private static int size[][];     // size[i,j] = number of sites in subtree rooted at i,j
	private static String filename = "input_3.txt";
	static double timerems = 0.0;
 
	protected static class Cell {
		int x;
		int y;
		int val;
		Cell(int a,int b,int c) {
			x=a;y=b;val=c;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + val;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			Cell other = (Cell) obj;
			if (val != other.val)
				return false;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}	
	}
	
	static double readInput() {
		BufferedReader br = null;
		double timerem =0;
		try {
			br = new BufferedReader(new FileReader(filename));    //("inputEx5_2.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			n = Integer.parseInt(br.readLine().trim());
			p = Integer.parseInt(br.readLine().trim());
			timerem = Double.parseDouble(br.readLine().trim());
			timerems = timerem;	
         	String line;
			for(int i =0; i <n; i++) {
				line = br.readLine();
				for(int j =0; j <n; j++) {
					if(line.charAt(j) == '*') {
						arr[i][j] = -1;
					}
					else
						arr[i][j] = line.charAt(j) - '0';
					//System.out.print(arr[i][j]);
				}
				//System.out.println();
			}
			return timerem;
		} catch(Exception e) {System.out.println("In readInput "+e.toString());  }
		return timerem;
	}

	public static long getCpuTime() {
	    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
	    return bean.isCurrentThreadCpuTimeSupported( ) ?
	        bean.getCurrentThreadCpuTime( ) : 0L;
	}

	static ArrayList<ArrayList<Cell>> makeClusters(int[][] arrmk) {
		
		int i=0, j=0;
		//Make union
		Cell[][] parent1 = null;
		WeightedQuickUnionUF(n,arrmk);
		
		parent1= new Cell[n][n];
		for ( i = 0; i < n; i++) {
			for ( j = 0; j < n; j++) {
				parent1[i][j] = new Cell(i,j,arrmk[i][j]); 
			}
		}
		
		for(i =0; i <n; i++) {
			for(j =0; j <n; j++) {
				if(arrmk[i][j]<0)
					continue;
				if(i>0 && (arrmk[i-1][j] == arrmk[i][j]) )
					union(new Cell(i-1,j,arrmk[i-1][j]),new Cell(i,j,arrmk[i][j]) ,parent1);
				if(j>0 && (arrmk[i][j-1] == arrmk[i][j]) )
					union(new Cell(i,j-1,arrmk[i][j-1]),new Cell(i,j,arrmk[i][j]), parent1 );
			}
		}
//		
		HashMap< Cell,ArrayList<Cell> > clusters= new HashMap< Cell,ArrayList<Cell> >();
		//Form hashmap of arraylist of cells with parent pos as key
		for(i =0; i <n; i++) {
			for(j =0; j <n; j++) {
				if(arrmk[i][j]<0)
					continue;
				Cell parentcell = find(parent1[i][j],parent1);

				if(clusters.containsKey(parentcell)) {
					ArrayList<Cell> list = clusters.get(parentcell);
					list.add(new Cell(i,j,arrmk[i][j]));
					//System.out.print("("+i+","+j+"):"+list.size()+" ");
				}
				else {
					ArrayList<Cell> list = new ArrayList<Cell>();
					list.add(new Cell(i,j,arrmk[i][j]));
					clusters.put(parentcell, list);
					//System.out.print("("+i+","+j+"):"+list.size()+" ");
				}
			}
			//System.out.println();	
		}
		
		//Add to arraylist of arraylist of clusters
//		clusList.clear();
		ArrayList<ArrayList<Cell>> clusList = new ArrayList<ArrayList<Cell>>();
		Iterator<Map.Entry<Cell,ArrayList<Cell>>> iter = clusters.entrySet().iterator();
		while(iter.hasNext()) {
			//cell c= iter.next().getKey();
			clusList.add(iter.next().getValue());
			//System.out.print("("+c.x+","+c.y+")"+clusters.get(c).size()+"  ");
		}
		
		//Sort by max size;
		Collections.sort(clusList,new myComparator());
//		for(i=0; i< clusList.size(); i++) {
//			System.out.print(" "+clusList.get(i).size());
//		}
//		System.out.println();
		return clusList;
	}

	protected static Object[] minimax(int arrorg[][],int player,int depth,int alp,int bet) {
		
		//if(movesrem ==0)
		//	return;
		Object[] retobj; // = new Object[2];
		
		int score;
		ArrayList<Cell> topcluster = null;
		if(depth == MAX_DEPTH+1) {
			score = evaluate(depth);
			retobj = new Object[]{score, topcluster};
			return retobj;
		}
		int[][] arr1 = new int[n][n];
		
		// Inits clusList and sorts it.
		//System.out.println("After make cluster:");
		ArrayList<ArrayList<Cell>> clusListcopy = makeClusters(arrorg);
		//System.out.println("ClusList size n depth: "+clusListcopy.size()+"  "+ depth);
		
		if(clusListcopy.size() ==0) {
			score = evaluate(depth);//Eval function;
			retobj = new Object[]{score, topcluster};
			return retobj;
		}
		
		for(ArrayList<Cell> list : clusListcopy ) {
			
			// Copy array 
			for(int i =0; i <n; i++) {
				for(int j =0; j <n; j++) {
					arr1[i][j] = arrorg[i][j];
				}
			}
			
			//Pick the max in applygravity
			applyGravity(arr1,list);
//			System.out.println("After Gravity");
//			
//			for(int i =0; i <n; i++) {
//				for(int j =0; j <n; j++) {
//					System.out.print(arr1[i][j]+" ");
//				}System.out.println();
//			}
			
			if(player == 1) {
//				scores[depth] = (int) Math.pow(list.size(),2);
//				System.out.println("ClusList size n depth: "+clusListcopy.size()+"  "+ depth);
				scores[depth] = list.size()*list.size();
				score = (int) minimax(arr1,0,depth+1,alp,bet)[0];
				if(score > alp) {
					alp=score;
					topcluster = list;
				}
			}
			else {
//				scores[depth] = -(int)Math.pow(list.size(),2);
				scores[depth] = -(list.size()*list.size());
				score = (int) minimax(arr1,1,depth+1,alp,bet)[0];
				if (score < bet) {
					bet = score;
					topcluster = list;
				}	
			}
			//System.out.println("SCOREEE at depth: "+ depth+ "is: " + score+ "========== Lsize"+ list.size());

			if (alp>= bet) break;                                 // || clusListcopy.indexOf(list)==7) break;
		}	
		retobj = new Object[]{player == 1?alp:bet, topcluster};
//		for(int i=0; i < MAX_DEPTH;i++)
//			System.out.print("Scores array: "+ scores[i]+" ");
		return retobj;
	}

	private static int evaluate(int depth) {
	int sum =0;
		for(int i=0;i<depth; i++)
		{  sum+=scores[i];}
		return sum;
	}

	private static void applyGravity(int arrcopy[][],ArrayList<Cell> list) {
		 
		if(list.size() < 1)
			return;
		//System.out.println(posString);
		for(int i =0; i <n; i++) {
			for(int j =0; j <n; j++) {
				for(int k=0; k< list.size(); k++) {
					Cell cl = list.get(k);
					if(( i==cl.x) && (j==cl.y)) { 
						arrcopy[i][j] = -2;  
						//System.out.print("("+cl.x+","+ cl.y+"), "); 
					}
				}
			}
		}

		//Applying gravity
		int count=0;
		for(int j =0; j <n; j++) {
			//count=0;
			for(int i= n-1; i > -1; i--) {
				if(arrcopy[i][j] == -2) {
					int k = i;
					while( k>=0 && arrcopy[k][j]==-2) {
						count++;
						k--;
					} /////////  If count ==n etc. n for 
					// System.out.println("Count:"+count); //+" k:"+k);
					int tcount = count;
					k=i;
					while( count>0 && (k-count) >= 0 ) {
						arrcopy[k][j]= arrcopy[k-count][j];
						k--;
					}
					for(int ii=0; ii<tcount;ii++) 
						arrcopy[ii][j]=-1;
				}
				count=0;
			}
		}		
		return;
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		long st = getCpuTime();
		double timerem = readInput();
		
		homework hw_Obj = new homework();
		Thread breakthread = new Thread(hw_Obj.new MyRunnable());
		//breakthread.start();	
     
     	ArrayList <ArrayList<Cell>> origClus = makeClusters(arr);
		int size = origClus.size();
		double ratio;
		ratio = timerem/size;
		if(timerem - 0.006<0) {
         System.exit(0);
        }
           
		if( (timerem - 0.600)< 0) {
			if(size >= 180)
				MAX_DEPTH = 0;
			else if (size < 30)
				MAX_DEPTH = 3;
			else
				MAX_DEPTH = 1;
		}
			
		else if( (timerem - 1.100)< 0) {
			if(size > 200)
				MAX_DEPTH = 1;
			else if(size<40)
				MAX_DEPTH = 3;
			else
				MAX_DEPTH = 2;
		}
		
		else if( (timerem - 3.000)< 0) {
			if(size >= 100 && size<300)
				MAX_DEPTH = 2;
			else if(size>=300)
				MAX_DEPTH = 1;
			else if(size<=20)
				MAX_DEPTH = 5;
			else
				MAX_DEPTH = 3;
		}
		else if (timerem - 15.000 < 0){
			if(size <= 40)
				MAX_DEPTH = 5;
			else if(size>40 && size <= 100)
				MAX_DEPTH = 3;
			else if(size>100 && size <= 200)
				MAX_DEPTH = 2;
			else if(size > 300)
				MAX_DEPTH =1;
			else
				MAX_DEPTH =2;
		}
		else if ( timerem - 25.000 < 0) {
			if(size>300)
				MAX_DEPTH = 1;
			else if(size <= 50)
				MAX_DEPTH = 3;
			else if(size>50 && size <= 100)
				MAX_DEPTH = 2;
			else
				MAX_DEPTH = 2;
		}
		else if( timerem - 125.000 < 0) {
			if(size>500)
				MAX_DEPTH = 1;
			else if(size <= 50)
				MAX_DEPTH = 4;
			else if(size>50 && size <= 100)
				MAX_DEPTH = 3;
			else if(size>100 && size <= 200)
				MAX_DEPTH = 2;
			else
				MAX_DEPTH = 2;
		}
		else {
			if(size>600)
				MAX_DEPTH = 1;
			else if(size <= 50)
				MAX_DEPTH = 4;
			else if(size>50 && size <= 100)
				MAX_DEPTH = 3;
			else if(size>100 && size <= 200)
				MAX_DEPTH = 2;
			else
				MAX_DEPTH = 2;
		}
//		if(ratio - 0.00500 < 0)
//			MAX_DEPTH = 2;
		
		// 1 is us. 0 is opponent
		scores = new int[8];

		int topscore; ArrayList<Cell> finalList = null;
		Object[] answer = new Object[2];
		answer = minimax(arr,1,0,Integer.MIN_VALUE, Integer.MAX_VALUE);
		
		topscore =(int) (answer[0]);

		finalList= (ArrayList<Cell>) answer[1];
		System.out.println("Finalscore: " +topscore);
		
		//for(int k=0; k< finalList.size(); k++) {
			Cell cl = null;  
			if(finalList != null) {
				cl=finalList.get(0);
				applyGravity(arr, finalList);
			}
			else
				cl = new Cell(n-1,n-1,0);
			//			System.out.print("("+cl.x+","+ cl.y+"), "); 
//			}
			char chosen;
			chosen = (char)(cl.y + 65);
			String posString =  chosen + Integer.toString((cl.x+1));
			
			FileWriter writer;
			BufferedWriter BW;
			try {
				writer = new FileWriter(new File("output.txt"));
				BW = new BufferedWriter(writer);
				BW.write(posString);
				BW.newLine();
				BW.flush();
				for(int i =0; i <n; i++) {
					for(int j =0; j <n; j++) {
						if(arr[i][j]==-1 ) {
							BW.write("*");
							continue;
						}
						BW.write(Integer.toString(arr[i][j]));
					}
					BW.newLine();
					BW.flush();
				}	
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		long endtime = System.currentTimeMillis();
		long et = getCpuTime();
		double cput= (et-st)/1000000;
		System.out.println("Pos is "+ posString + " Size is "+ size+ "  Time is "+ (endtime-startTime)+ " MAx depth is:"+ MAX_DEPTH);
		System.out.println("CPU time is "+ cput+ " ratio of timerem is "+ ratio*1000+ ", taken is "+(cput/size));
     	System.exit(0);
	}

 	public class MyRunnable implements Runnable {
		@Override
		public void run() {
			try {
				Thread.sleep(((long)timerems) + 400);
				System.exit(0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
	}
 
	static class myComparator implements Comparator<ArrayList<Cell>> {
		@Override
		public int compare(ArrayList<Cell> A1, ArrayList<Cell> A2) {
			return (A2.size()-A1.size());
		}
	}

	/**
	 * Initializes an empty union–find data structure with {@code n} sites
	 * {@code 0} through {@code n-1}. Each site is initially in its own 
	 * component.
	 *
	 * @param  n the number of sites
	 * @return 
	 */
	public static void WeightedQuickUnionUF(int n,int[][]arrPar) {
		//par = new Cell[n][n];
		size = new int[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				//par[i][j] = new Cell(i,j,arrPar[i][j]);
				size[i][j] = 1;
			}
		}
		return;
	}

	/**
	 * Returns the component identifier for the component containing site {@code p}.
	 *
	 * @param  p the integer representing one object
	 * @return the component identifier for the component containing site {@code p}
	 * @throws IllegalArgumentException unless {@code 0 <= p < n}
	 */
	public static Cell find(Cell p,Cell par[][]) {
		//int parentx = parent[p.x][p.y].x;
		//int parenty = parent[p.x][p.y].y;
		//while (! ((p.x == parentx) && (p.y == parenty)) )
		while( p != par[p.x][p.y])
			p = par[p.x][p.y];
		return p;
	}

	public static void union(Cell p, Cell q, Cell[][] par) {
		//try {
			Cell rootP = find(p,par);
			Cell rootQ = find(q,par);
			if (rootP == rootQ) return;

			// make smaller root point to larger one
			if (size[rootP.x][rootP.y] < size[rootQ.x][rootQ.y]) {
				par[rootP.x][rootP.y] = rootQ;
				size[rootQ.x][rootQ.y] += size[rootP.x][rootP.y];
				//size[rootP.x][rootP.y] = size[rootQ.x][rootQ.y];
			}
			else {
				par[rootQ.x][rootQ.y] = rootP;
				size[rootP.x][rootP.y] += size[rootQ.x][rootQ.y];
				//size[rootQ.x][rootQ.y] = size[rootP.x][rootP.y];
			}
		//} catch(Exception e) { e.printStackTrace(); System.exit(5);}
	}

}
