//=======================================================================
// Copyright 2013 University of Warsaw.
// Authors: Piotr Wygocki 
//
// Distributed under the Boost Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)
//=======================================================================
//
//This algorithm is described in "Network Flows: Theory, Algorithms, and Applications"
// by Ahuja, Magnanti, Orlin.

#ifndef BOOST_GRAPH_SUCCESSIVE_SHORTEST_PATH_HPP
#define BOOST_GRAPH_SUCCESSIVE_SHORTEST_PATH_HPP 

#include <numeric>

#include <boost/property_map/property_map.hpp>
#include <boost/graph/graph_traits.hpp>
#include <boost/graph/graph_concepts.hpp>
#include <boost/pending/indirect_cmp.hpp>
#include <boost/pending/relaxed_heap.hpp>
//#include <boost/graph/dijkstra_shortest_paths.hpp>
#include <boost/graph/properties.hpp>
#include <boost/graph/iteration_macros.hpp>
#include <boost/graph/detail/augment.hpp>
#include <boost/graph/bellman_ford_shortest_paths.hpp>
#include <boost/graph/copy.hpp>
#include <boost/graph/find_flow_cost.hpp>


namespace Graphs {


	namespace detail {


		template <class Graph, class Weight, class Distance, class Reversed>
		class MapReducedWeight :
			public put_get_helper<typename property_traits<Weight>::value_type, MapReducedWeight<Graph, Weight, Distance, Reversed> > {

			typedef graph_traits<Graph> gtraits;

			//typedef	typename property_map < Graph, edge_residual_capacity_t >::type residual_capacity;
		public:
			typedef boost::readable_property_map_tag category;
			typedef typename property_traits<Weight>::value_type value_type;
			typedef value_type reference;
			typedef typename gtraits::edge_descriptor key_type;



			MapReducedWeight(const Graph & g, Weight w, Distance d, Reversed r) :
				g_(g), weight_(w), distance_(d), rev_(r) {

			}

			reference operator[](key_type v) const {

				//residual_capacity	res= get(edge_capacity_t, g_);
				//	int val = int(g_[v].edge_residual_capacity_t);
				//	if (val){
				return get(distance_, source(v, g_)) - get(distance_, target(v, g_)) + get(weight_, v);
				//	}
				//	else{
				//		return std::numeric_limits < short >::max();
				//	}

			}
		private:
			const Graph & g_;
			Weight weight_;
			Distance distance_;
			Reversed rev_;
		};


		template <class Graph, class Weight, class Distance, class Reversed>
		MapReducedWeight<Graph, Weight, Distance, Reversed>
			make_mapReducedWeight(const Graph & g, Weight w, Distance d, Reversed r)  {
				return MapReducedWeight<Graph, Weight, Distance, Reversed>(g, w, d, r);
		}

	}//detail


	template <class Graph, class Capacity, class ResidualCapacity, class Reversed, class Pred, class Weight, class Distance, class Distance2, class VertexIndex>
	void successive_shortest_path(
		const Graph &g,
		typename graph_traits<Graph>::vertex_descriptor s,
		typename graph_traits<Graph>::vertex_descriptor t,
		Capacity capacity,
		ResidualCapacity residual_capacity,
		Weight weight,
		Reversed rev,
		VertexIndex index,
		Pred pred,
		Distance distance,
		Distance2 distance_prev) {
		filtered_graph<const Graph, is_residual_edge<ResidualCapacity> >
			gres = boost::detail::residual_graph(g, residual_capacity);
		typedef typename graph_traits<Graph>::edge_descriptor edge_descriptor;




		BGL_FORALL_EDGES_T(e, g, Graph) {
			put(residual_capacity, e, get(capacity, e));
		}

		BGL_FORALL_VERTICES_T(v, g, Graph) {
			put(distance_prev, v, 0);
		}




		for (int i = 0; i < 15; i++){

			BGL_FORALL_VERTICES_T(v, g, Graph) {
				put(pred, v, edge_descriptor());
			}

			BGL_FORALL_VERTICES_T(v, g, Graph) {
				put(distance, v, (std::numeric_limits < short >::max)());
			}
			put(distance, s, 0);

			boost::bellman_ford_shortest_paths(gres, num_vertices(g),
				weight_map(detail::make_mapReducedWeight(gres, weight, distance_prev, rev)).
				distance_map(distance).
				vertex_index_map(index).
				visitor(boost::make_bellman_visitor(record_edge_predecessors(pred, on_edge_relaxed()))));


			if (get(pred, t) == edge_descriptor()) {
				break;
			}

			BGL_FORALL_VERTICES_T(v, g, Graph) {
				put(distance_prev, v, get(distance_prev, v) + get(distance, v));
			}

			boost::detail::augment(g, s, t, pred, residual_capacity, rev);
		}

		int cost = boost::find_flow_cost(g);
		int cost2 = cost - 1;
		while (cost2 < cost){

			//std::cout << "cost loop: " << cost << std::endl;
			BGL_FORALL_VERTICES_T(v, g, Graph) {
				put(pred, v, edge_descriptor());
			}

			BGL_FORALL_VERTICES_T(v, g, Graph) {
				put(distance, v, (std::numeric_limits < short >::max)());
			}
			put(distance, s, 0);


			boost::bellman_ford_shortest_paths(gres, num_vertices(g),
				weight_map(detail::make_mapReducedWeight(gres, weight, distance_prev, rev)).
				distance_map(distance).
				vertex_index_map(index).
				visitor(boost::make_bellman_visitor(record_edge_predecessors(pred, on_edge_relaxed()))));



			if (get(pred, t) == edge_descriptor()) {
				break;
			}

			BGL_FORALL_VERTICES_T(v, g, Graph) {
				put(distance_prev, v, get(distance_prev, v) + get(distance, v));
			}

			boost::detail::augment(g, s, t, pred, residual_capacity, rev);

			cost = cost2;
			cost2 = boost::find_flow_cost(g);
		}

	}

	//in this namespace argument dispatching tak place
	namespace detail {

		template <class Graph, class Capacity, class ResidualCapacity, class Weight, class Reversed, class Pred, class Distance, class Distance2, class VertexIndex>
		void successive_shortest_path_dispatch3(
			const Graph &g,
			typename graph_traits<Graph>::vertex_descriptor s,
			typename graph_traits<Graph>::vertex_descriptor t,
			Capacity capacity,
			ResidualCapacity residual_capacity,
			Weight weight,
			Reversed rev,
			VertexIndex index,
			Pred pred,
			Distance dist,
			Distance2 dist_pred) {
			successive_shortest_path(g, s, t, capacity, residual_capacity, weight, rev, index, pred, dist, dist_pred);
		}

		//setting default distance map
		template <class Graph, class Capacity, class ResidualCapacity, class Weight, class Reversed, class Pred, class Distance, class VertexIndex>
		void successive_shortest_path_dispatch3(
			Graph &g,
			typename graph_traits<Graph>::vertex_descriptor s,
			typename graph_traits<Graph>::vertex_descriptor t,
			Capacity capacity,
			ResidualCapacity residual_capacity,
			Weight weight,
			Reversed rev,
			VertexIndex index,
			Pred pred,
			Distance dist,
			param_not_found) {
			typedef typename property_traits<Weight>::value_type D;

			std::vector<D> d_map(num_vertices(g));

			successive_shortest_path(g, s, t, capacity, residual_capacity, weight, rev, index, pred, dist,
				make_iterator_property_map(d_map.begin(), index));
		}

		template <class Graph, class P, class T, class R, class Capacity, class ResidualCapacity, class Weight, class Reversed, class Pred, class Distance, class VertexIndex>
		void successive_shortest_path_dispatch2(
			Graph &g,
			typename graph_traits<Graph>::vertex_descriptor s,
			typename graph_traits<Graph>::vertex_descriptor t,
			Capacity capacity,
			ResidualCapacity residual_capacity,
			Weight weight,
			Reversed rev,
			VertexIndex index,
			Pred pred,
			Distance dist,
			const bgl_named_params<P, T, R>& params) {
			successive_shortest_path_dispatch3(g, s, t, capacity, residual_capacity, weight, rev, index, pred, dist, get_param(params, vertex_distance2));
		}

		//setting default distance map
		template <class Graph, class P, class T, class R, class Capacity, class ResidualCapacity, class Weight, class Reversed, class Pred, class VertexIndex>
		void successive_shortest_path_dispatch2(
			Graph &g,
			typename graph_traits<Graph>::vertex_descriptor s,
			typename graph_traits<Graph>::vertex_descriptor t,
			Capacity capacity,
			ResidualCapacity residual_capacity,
			Weight weight,
			Reversed rev,
			VertexIndex index,
			Pred pred,
			param_not_found,
			const bgl_named_params<P, T, R>& params) {
			typedef typename property_traits<Weight>::value_type D;

			std::vector<D> d_map(num_vertices(g));

			successive_shortest_path_dispatch3(g, s, t, capacity, residual_capacity, weight, rev, index, pred,
				make_iterator_property_map(d_map.begin(), index),
				get_param(params, vertex_distance2));
		}

		template <class Graph, class P, class T, class R, class Capacity, class ResidualCapacity, class Weight, class Reversed, class Pred, class VertexIndex>
		void successive_shortest_path_dispatch1(
			Graph &g,
			typename graph_traits<Graph>::vertex_descriptor s,
			typename graph_traits<Graph>::vertex_descriptor t,
			Capacity capacity,
			ResidualCapacity residual_capacity,
			Weight weight,
			Reversed rev,
			VertexIndex index,
			Pred pred,
			const bgl_named_params<P, T, R>& params) {
			successive_shortest_path_dispatch2(g, s, t, capacity, residual_capacity, weight, rev, index, pred,
				get_param(params, vertex_distance), params);
		}

		//setting default predecessors map
		template <class Graph, class P, class T, class R, class Capacity, class ResidualCapacity, class Weight, class Reversed, class VertexIndex>
		void successive_shortest_path_dispatch1(
			Graph &g,
			typename graph_traits<Graph>::vertex_descriptor s,
			typename graph_traits<Graph>::vertex_descriptor t,
			Capacity capacity,
			ResidualCapacity residual_capacity,
			Weight weight,
			Reversed rev,
			VertexIndex index,
			param_not_found,
			const bgl_named_params<P, T, R>& params) {
			typedef typename graph_traits<Graph>::edge_descriptor edge_descriptor;
			std::vector<edge_descriptor> pred_vec(num_vertices(g));

			successive_shortest_path_dispatch2(g, s, t, capacity, residual_capacity, weight, rev, index,
				make_iterator_property_map(pred_vec.begin(), index),
				get_param(params, vertex_distance), params);
		}

	}//detail


	template <class Graph, class P, class T, class R>
	void successive_shortest_path(
		Graph &g,
		typename graph_traits<Graph>::vertex_descriptor s,
		typename graph_traits<Graph>::vertex_descriptor t,
		const bgl_named_params<P, T, R>& params) {

		return detail::successive_shortest_path_dispatch1(g, s, t,
			choose_const_pmap(get_param(params, edge_capacity), g, edge_capacity),
			choose_pmap(get_param(params, edge_residual_capacity),
			g, edge_residual_capacity),
			choose_const_pmap(get_param(params, edge_weight), g, edge_weight),
			choose_const_pmap(get_param(params, edge_reverse), g, edge_reverse),
			choose_const_pmap(get_param(params, vertex_index), g, vertex_index),
			get_param(params, vertex_predecessor),
			params);
	}

	template <class Graph>
	void successive_shortest_path(
		Graph &g,
		typename graph_traits<Graph>::vertex_descriptor s,
		typename graph_traits<Graph>::vertex_descriptor t) {
		bgl_named_params<int, buffer_param_t> params(0);
		successive_shortest_path(g, s, t, params);
	}


}//boost
#endif /* BOOST_GRAPH_SUCCESSIVE_SHORTEST_PATH_HPP */
