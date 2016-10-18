package nl.vu.cs.lsde.flightroutes

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD

object HelloApp {
  private val threshold = 250
  private val insdel_weight = 10
  private val editing_weight = 1

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("boh")
    val sc = new SparkContext(conf)

    val csv1 = sc.textFile(args(0) + "/3c48a2-3")
    val csv2 = sc.textFile(args(0) + "/3c48a2-4")
    val csv3 = sc.textFile(args(0) + "/3c48a2-5")
    val csv4 = sc.textFile(args(0) + "/3c48a2-6")

    val csv = csv1 ++ csv2 ++ csv3 ++ csv4

    val data = csv map (line => {
      val row = line.split(",")
      (row(0),(row(5).toDouble, row(6).toFloat,row(7).toFloat))
    }) groupByKey

    val flights = data map (_._2.toArray.sortBy(_._1).map(el => (el._2,el._3))) map points2route

    val clusters = clusterize(flights.collect.toList)

    //sc.parallelize(clusters).saveAsTextFile(args(0) + "/out")

    val zippedFlights = flights.collect.toList.zipWithIndex

    for (f1 <- zippedFlights; f2 <- zippedFlights) {
      println(f1._2.toString + "," + f2._2.toString + ": " + distance(f1._1, f2._1).toString)
    }


    println("*********************************")
    println(clusters)
    println("*********************************")
    println("number of clusters:", clusters.length)
    println("*********************************")
    println(clusters.head._2)
    println("*********************************")
    println("number of routes in first cluster:", clusters.head._2.length)
    println("*********************************")
    clusters.head._2.zipWithIndex.foreach(route =>
      println("route " + route._2 + ": " + route._1.length)
    )
    println("*********************************")

  }

  def points2route(points: Array[(Float, Float)]): Array[(Int,Int)] =
    points map (z => (z._1.*(10).toInt, z._2.*(10).toInt)) distinct

  def distance(route1: Array[(Int,Int)], route2: Array[(Int,Int)]): Int = {
    val dist = Array.tabulate(route1.length+1, route2.length+1) {
      (j,i) => if (j==0) i else if (i==0) j else 0
    }

    for(j <- 1 to route1.length; i <- 1 to route2.length) {
      lazy val diff = (route1(j-1)._1 - route2(i-1)._1, route1(j-1)._2 - route2(i-1)._2)
      dist(j)(i) = if (route1(j-1) == route2(i-1))
                    dist(j-1)(i-1)
                  else
                    Array(
                      dist(j-1)(i) + insdel_weight,
                      dist(j)(i-1) + insdel_weight,
                      dist(j-1)(i-1) + (editing_weight * (diff._1*diff._1 + diff._2*diff._2))
                    ).min
    }

    dist(route1.length)(route2.length)
  }

  def clusterize(routes: List[Array[(Int,Int)]]): List[(Int, List[Array[(Int,Int)]])] = {
    if (routes isEmpty) return Nil

    val (cluster, remaining) = clusterize_rec((1, List(routes head)), routes tail)

    cluster :: clusterize(remaining)
  }

  private def clusterize_rec(cluster: (Int, List[Array[(Int,Int)]]), routes: List[Array[(Int,Int)]]): ((Int, List[Array[(Int,Int)]]), List[Array[(Int,Int)]]) = {
    val (add, others) = routes.partition(route =>
      cluster._2.exists(r =>
        distance(route, r) <= threshold
      )
    )
    if (add.length > 0)
      clusterize_rec((cluster._1 + add.length, cluster._2 ::: add), others)
    else
      (cluster, routes)
  }

  def leaderize(cluster: List[Array[(Int, Int)]]): Array[(Int, Int)] = {
    val zippedCluster = cluster.zipWithIndex;
    val distances = for (r1 <- zippedCluster; r2 <- zippedCluster) yield (r1._2, distance(r1._1, r2._1))
    val pippo = distances.groupBy(_._1).map(cp => (cp._1, cp._2.reduce(_._2 + _._2)))
    val min = pippo.minBy(_._2)._1
    zippedCluster(min)._1
  }
}
