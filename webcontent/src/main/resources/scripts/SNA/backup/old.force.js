


var w = 800,
    h = 500

var vis = d3.select("#chart")
	.append("svg:svg")
    .attr("width", w)
    .attr("height", h);

 var color = d3.scale.category20();

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

	
	
	
    var force = self.force = d3.layout.force()
        .nodes(json.nodes)	
        .links(json.links)
        .gravity(.05)
        .distance(100)
        .charge(-100)
        .size([w, h])
        .start();

    var link = vis.selectAll("line.link")
        .data(json.links)
        .enter().append("svg:line")
        .attr("class", "link")
        .attr("x1", function(d) { return d.source.x; })
        .attr("y1", function(d) { return d.source.y; })
        .attr("x2", function(d) { return d.target.x; })
        .attr("y2", function(d) { return d.target.y; });

    var node_drag = d3.behavior.drag()
        .on("dragstart", dragstart)
        .on("drag", dragmove)
        .on("dragend", dragend);

    function dragstart(d, i) {
        force.stop() // stops the force auto positioning before you start dragging
    }

    function dragmove(d, i) {
        d.px += d3.event.dx;
        d.py += d3.event.dy;
        d.x += d3.event.dx;
        d.y += d3.event.dy; 
        tick(); // this is the key to make it work together with updating both px,py,x,y on d !
    }

    function dragend(d, i) {
        d.fixed = true; // of course set the node to fixed so the force doesn't include the node in its auto positioning stuff
        tick();
        force.resume();
    }


    var node = vis.selectAll("g.node")
        .data(json.nodes)
		//.key(function(d) {return d.id;})
		//.data(json.nodes, function(d) { return d.id; });
	
       .enter().append("svg:g")
        .attr("class", "node")
	.style("fill", function(d) { return color(d.KmeansClusterGroup); })
        .call(node_drag);

    node.append("svg:circle")
        .attr("class", "circle")
        .attr("r", "6")

    node.append("svg:text")
        .attr("class", "nodetext")
        .attr("dx", 12)
        .attr("dy", ".35em")
        .text(function(d) { return d.name });

    force.on("tick", tick);

    function tick() {

	
      link.attr("x1", function(d) { return d.source.x; })
          .attr("y1", function(d) { return d.source.y; })
          .attr("x2", function(d) { return d.target.x; })
          .attr("y2", function(d) { return d.target.y; });

      node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });

    };


});
