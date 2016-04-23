var w = 960,
    h = 500;

var vis = d3.select("#chart")
	.append("svg:svg")
	.attr("width", w)
	.attr("height", h);
	
var color = d3.scale.category20();

d3.json("/SNA/data.json", function(json) {
var nodes = json.nodes;

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

var links = json.links;



var force = self.force = d3.layout.force()
    .nodes(nodes)
    .links(links)
    .size([w, h])
    .gravity(.05)
    .distance(200)
    .charge(-100)
    //.on("tick", tick)
    .start();

// Per-type markers, as they don't inherit styles.
vis.append("svg:defs").selectAll("marker")
    .data(["Slave sale", "Slave mark", "Ownership", "House sale"])
  .enter().append("svg:marker")
    .attr("id", String)
    .attr("viewBox", "0 -5 10 10")
    .attr("refX", 15)
    .attr("refY", -1.5)
    .attr("markerWidth", 6)
    .attr("markerHeight", 6)
    .attr("orient", "auto")
  .append("svg:path")
    .attr("d", "M0,-5L10,0L0,5");

var path = vis.append("svg:g").selectAll("path")
    .data(force.links())
  .enter().append("svg:path")
    .attr("class", function(d) { return "link " + d.type; })
	.attr("fill", "none")
	.attr("stroke", function(d) { return color(d.type); })
    .attr("marker-end", function(d) { return "url(#" + d.type + ")"; });
	
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

var text = vis.append("svg:g").selectAll("g")
    .data(force.nodes())
  .enter().append("svg:g");

// A copy of the text with a thick white stroke for legibility.
text.append("svg:text")
    .attr("x", 8)
    .attr("y", ".31em")
    .attr("class", "shadow")
    .text(function(d) { return d.name; });

text.append("svg:text")
    .attr("x", 8)
    .attr("y", ".31em")
    .text(function(d) { return d.name; });
	
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


    force.on("tick", tick);

function tick() {
	
	
  path.attr("d", function(d) {
    var dx = d.target.x - d.source.x,
        dy = d.target.y - d.source.y,
        dr = Math.sqrt(dx * dx + dy * dy);
    return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
  });


  node.attr("transform", function(d) {return "translate(" + d.x + "," + d.y + ")";})
	//.attr("cx", function(d) { return d.x = Math.max(r, Math.min(w - r, d.x)); })
	//.attr("cy", function(d) { return d.y = Math.max(r, Math.min(h - r, d.y)); });
	

  text.attr("transform", function(d) {
    return "translate(" + d.x + "," + d.y + ")";
  });
}

});