var w = 960,
    h = 500,
    fill = d3.scale.category10();


d3.json("/SNA/data.json", function(json) {

	//create a map of the original array using IDs as keys
	var nodesById= {};
	
	json.nodes.forEach(function(node){
		nodesById[node.id]=node;
	});
	
	//modifications to the array happen here
	// Create nodes for each unique source and target.
	 json.links.forEach(function(link) {
	    link.source = nodesById[link.source];
	    link.target = nodesById[link.target];
	  });
	
	
	var groups = d3.nest()
	    .key(function(d) { return d.KmeansClusterGroup; })
	    .entries(json.nodes);
	

	var groupPath = function(d) {
	    return "M" + 
	      d3.geom.hull(d.values.map(function(i) { return [i.x, i.y]; }))
	        .join("L")
	    + "Z";
	};	
	
	var groupFill = function(d, i) { return fill(i & 3); };
	
	var vis = d3.select("#hulls").append("svg")
	    .attr("width", w)
	    .attr("height", h);

	var force = d3.layout.force()
	    .nodes(json.nodes)
	    .links(json.links)
	    .size([w, h])
	    .start();

	var node = vis.selectAll("circle.node")
	    .data(json.nodes)
	  .enter().append("circle")
	    .attr("class", "node")
	    .attr("cx", function(d) { return d.x; })
	    .attr("cy", function(d) { return d.y; })
	    .attr("r", 5)
	    .style("fill", function(d, i) { return fill(i & 3); })
	    .style("stroke", function(d, i) { return d3.rgb(fill(i & 3)).darker(2); })
	    .style("stroke-width", 1.5)
	    .call(force.drag);

	vis.style("opacity", 1e-6)
	  .transition()
	    .duration(1000)
	    .style("opacity", 1);

	force.on("tick", function(e) {

	  // Push different nodes in different directions for clustering.
	  var k = 6 * e.alpha;
	  json.nodes.forEach(function(o, i) {
	    o.x += i & 2 ? k : -k;
	    o.y += i & 1 ? k : -k;
	  });

	  node.attr("cx", function(d) { return d.x; })
	      .attr("cy", function(d) { return d.y; });

	  vis.selectAll("path")
	    .data(groups)
	      .attr("d", groupPath)
	    .enter().insert("path", "circle")
	      .style("fill", groupFill)
	      .style("stroke", groupFill)
	      .style("stroke-width", 40)
	      .style("stroke-linejoin", "round")
	      .style("opacity", .2)
	      .attr("d", groupPath);
	});

	d3.select("body").on("click", function() {
	  json.nodes.forEach(function(o, i) {
	    o.x += (Math.random() - .5) * 40;
	    o.y += (Math.random() - .5) * 40;
	  });
	  force.resume();
	});
   


});
