//BPS VIZ TOOLKIT MAIN

var Network, RadialPlacement, activate, root;

root = typeof exports !== "undefined" && exports !== null ? exports : this;

// Help with the placement of nodes
RadialPlacement = function() {
  var center, current, increment, place, placement, radialLocation, radius, setKeys, start, values;
  // stores the key -> location values
  values = d3.map();
  // how much to separate each location by
  increment = 20;
  // how large to make the layout
  radius = 200;
  // where the center of the layout should be
  center = {
    "x": 0,
    "y": 0
  };
  // what angle to start at
  start = -120;
  current = start;

  // Given an center point, angle, and radius length,
  // return a radial position for that angle
  radialLocation = function(center, angle, radius) {
    var x, y;
    x = center.x + radius * Math.cos(angle * Math.PI / 180);
    y = center.y + radius * Math.sin(angle * Math.PI / 180);
    return {
      "x": x,
      "y": y
    };
  };

  // Main entry point for RadialPlacement
  // Returns location for a particular key,
  // creating a new location if necessary.
  placement = function(key) {
    var value;
    value = values.get(key);
    if (!values.has(key)) {
      value = place(key);
    }
    return value;
  };

  // Gets a new location for input key
  place = function(key) {
    var value;
    value = radialLocation(center, current, radius);
    values.set(key, value);
    current += increment;
    return value;
  };

  // Given a set of keys, perform some 
  // magic to create a two ringed radial layout.
  // Expects radius, increment, and center to be set.
  // If there are a small number of keys, just make
  // one circle.
  setKeys = function(keys) {
    var firstCircleCount, firstCircleKeys, secondCircleKeys;
    // start with an empty values
    values = d3.map();

    // number of keys to go in first circle
    firstCircleCount = 360 / increment;

    // if we do not have enough keys, modify increment
    // so that they all fit in one circle
    if (keys.length < firstCircleCount) {
      increment = 360 / keys.length;
    }

    // set locations for inner circle
    firstCircleKeys = keys.slice(0, firstCircleCount);
    firstCircleKeys.forEach(function(k) {
      return place(k);
    });

    // set locations for outer circle
    secondCircleKeys = keys.slice(firstCircleCount);

    // setup outer circle
    radius = radius + radius / 1.8;
    increment = 360 / secondCircleKeys.length;
    return secondCircleKeys.forEach(function(k) {
      return place(k);
    });
  };


  placement.keys = function(_) {
    if (!arguments.length) {
      return d3.keys(values);
    }
    setKeys(_);
    return placement;
  };
  placement.center = function(_) {
    if (!arguments.length) {
      return center;
    }
    center = _;
    return placement;
  };
  placement.radius = function(_) {
    if (!arguments.length) {
      return radius;
    }
    radius = _;
    return placement;
  };
  placement.start = function(_) {
    if (!arguments.length) {
      return start;
    }
    start = _;
    current = start;
    return placement;
  };
  placement.increment = function(_) {
    if (!arguments.length) {
      return increment;
    }
    increment = _;
    return placement;
  };
  return placement;
};

Network = function() {
  // variables we want to access
  // in multiple places of Network
  var allData, charge, curLinksData, curNodesData, filter, filterLinks, NodeTextTick,
      filterNodes, force, forceTick, groupCenters, height, hideDetails, markers, legend, legendG,
      layout, link, linkedByIndex, linksG, mapNodes, moveToRadialLayout, forceCharge, firstClick,
	  neighboring, network, node, nodeColors, nodeCounts, nodesG, radialTick, paused, nodeColorBinding,
	  setFilter, setLayout, setSort, setupData, showDetails, sort, sortedCategories, VizEdges,
	  strokeFor, tooltip, update, updateCenters, updateLinks, updateNodes, width;
	
  width = 800;
  height = 600;
  // allData will store the unfiltered data
  allData = [];
  curLinksData = [];
  curNodesData = [];
  linkedByIndex = {};
  VizEdges = true;

	//we start with text next to the nodes
	NodeTextTick = true;
	drawLegendFlag = true;
	paused = false;
   firstClick= true;
	
  // these will hold the svg groups for
  // accessing the nodes and links display
  nodesG = null;
  linksG = null;
  markers = null;
  legendG = null;

  // these will point to the circles and lines
  // of the nodes and links
  node = null;
  link = null;
  
  // variables to reflect the current settings
  // of the visualization
  layout = "force";
  filter = "all";
  sort = "cluster";
  nodeColorBinding = "cluster";

  // groupCenters will store our radial layout 
  groupCenters = null;

  // our force directed layout
  force = d3.layout.force();
  // color function used to color nodes
  nodeColors = d3.scale.category20();
  // tooltip used to display details
  tooltip = Tooltip("vis-tooltip", 230);

  // charge used in radial layout
  charge = function(node) {
    return -Math.pow(node.radius, 2.0) / 2;
  };

  forceCharge = -200;

  // Starting point for network visualization
  // Initializes visualization and starts force layout
  network = function(selection, data) {
    var vis;
	
    // format our data
    allData = setupData(data);

    // create our svg and groups
    vis = d3.select(selection).append("svg").attr("width", width).attr("height", height);
    linksG = vis.append("g").attr("id", "links");
    nodesG = vis.append("g").attr("id", "nodes");
	legendG = vis.append("g").attr("id", "legend");
	
	markers= vis.append("svg:defs").selectAll("marker")
	   .data(["true"])
	  .enter().append("svg:marker")
	    .attr("id", String)
	    .attr("viewBox", "0 -5 10 10")
	    .attr("refX", function(d) { 
		    // Needs a fudge-factor to handle the larger diameter circles 
		    return 15; 
		  })
	    .attr("refY", 1.5)
	    .attr("markerWidth", 6)
	    .attr("markerHeight", 6)
	    .attr("orient", "auto")
	  .append("svg:path")
	    .attr("d", "M0,-5L10,0L0,5");
	
    // setup the size of the force environment
    force.size([width, height]);
    setLayout("force");
    setFilter("all");
	
    // perform rendering and start force layout
    return update();
  };


  // The update() function performs the bulk of the
  // work to setup our visualization based on the
  // current layout/sort/filter.
  //
  // update() is called everytime a parameter changes
  // and the network needs to be reset.

  update = function() {
    var categories;

	// filter data to show based on current filter settings.
    curNodesData = filterNodes(allData.nodes);
    curLinksData = filterLinks(allData.links, curNodesData);

	// sort nodes based on current sort and update centers for
    // radial layout
    if (layout === "radial") {
      categories = sortedCategories(curNodesData, curLinksData);
      updateCenters(categories);
    }
	else if (layout ==="force") force.charge(forceCharge);

	// reset nodes in force layout
    force.nodes(curNodesData);

	// enter / exit for nodes
    updateNodes();

	 // always show links in force layout
    if (layout === "force") {
      
	  force.links(curLinksData);
      updateLinks();	
    } else {
		// reset links so they do not interfere with
	    // other layouts. updateLinks() will be called when
	    // force is done animating.
		force.links([]);
      if (link) {
		// if present, remove them from svg 
        link.data([]).exit().remove();
        link = null;
      }
    }
	
	drawLegend();
	
	// start me up!
	if (!paused) return force.start();
	else return force.stop();
  };

  drawLegend = function() {
		legendG.selectAll("g").remove();
	
		var docList = new Array();
		curLinksData.forEach(function(link){
			docList.push(link.type);
		});

		docList = $.grep(docList, function(v, k){
		    return $.inArray(v ,docList) === k;
		});
		
		if (docList.length<2) drawLegendFlag= false;
		else drawLegendFlag = true;
		
		if (drawLegendFlag){
			
		legend = legendG.selectAll("g.legend").data(docList);
		
		
		legend
		.enter()
		.append("g")
			.append("rect")
				.attr("width", "40").attr("height", "20")
				.attr("y", function (d, i) {
				                return (i + 1) * 20;
				             })
				.attr("fill", function(d) {
			      return nodeColors(d);
			    	})
	
		  legend.append("text").attr("class", "text")
				          .attr("x", function(d, i) { return 42; } )
				          .attr("y", function (d, i) {
							                return (i + 1.5) * 20;
							             })
					  .attr("text-anchor", function(d) { return "start";} )
					  .attr("font-family", "Arial, Helvetica, sans-serif")
				          .style("font", "normal 11px Arial")
				          .attr("fill", function(d) {
					      return nodeColors(d);
					    	})
				          .text(function(d) { return d; }) ;
				    legend
    .on("mouseover", function(d){
      linksG.selectAll("path."+ d).style("stroke-width", 2.0);
    })
    .on("mouseout", function(d){
      linksG.selectAll("path."+ d).style("stroke-width", 1.0);
    }); 

        drawLegendFlag= false;


				}		
		else {
      if (legend!= null) legend.remove();
    
			}
		
      if (legend==null) return null;


		return legend.exit().remove();

}

   network.changeForce = function(value){
	force.stop(); 
	forceCharge = Math.abs(value) * -1;
	 return update();
   };

	network.setNodeText = function(value){
		force.stop(); 
		if (value ==="NodeNamesOn") NodeTextTick = true;
		else if (value === "NodeNamesOff")NodeTextTick = false;
		 return update();
	  };
	  
	network.setEdgesVisibile = function(value){
		force.stop(); 
		if (value ==="EdgesOn") VizEdges = true;
		else if (value === "EdgesOff") VizEdges = false;
		 return update();
	  };
	  
	  
   network.setNodeColor = function(value){
		force.stop(); 
		nodeColorBinding = value;
		return update();
	  };

// Public function to switch between layouts
  network.toggleLayout = function(newLayout) {
    force.stop();
    setLayout(newLayout);
    return update();
  };

 // Public function to switch between filter options
  network.toggleFilter = function(newFilter) {
    force.stop();
    setFilter(newFilter);
    return update();
  };

  // Public function to switch between sort options
  network.toggleSort = function(newSort) {
    force.stop();
    setSort(newSort);
    return update();
  };

  network.toggleForce = function(val) {
    if (val === "ForceStop") paused= true;
	else if (val === "ForceStart") paused = false;
	update();
  };

  // Public function to update highlighted nodes
  // from search
  network.updateSearch = function(searchTerm) {
    var searchRegEx;
    searchRegEx = new RegExp(searchTerm.toLowerCase());
    return node.each(function(d) {
      var element, match;
      element = d3.select(this);
      match = d.name.toLowerCase().search(searchRegEx);
      if (searchTerm.length > 0 && match >= 0) {
        element.select("circle").style("fill", "#F38630").style("stroke-width", 2.0).style("stroke", "#555");
        return d.searched = true;
      } else {
        d.searched = false;
        return element.select("circle").style("fill", function(d) {
          return nodeColors(d.status);
        }).style("stroke-width", 1.0);
      }
    });
  };


  network.updateData = function(newData) {
    allData = setupData(newData);
	if(link)
		link.remove();
	if(node)
		node.remove();
	if(legend)
		legend.remove();
    return update();
  };


  // called once to clean up raw data and switch links to
  // point to node instances
  // returns modified data
  setupData = function(data) {
    var circleRadius, countExtent, nodesMap;
	// Data binding happens here
    
    
    
    countExtent = d3.extent(data.nodes, function(d) {
      return d.docID;
    });
    circleRadius = d3.scale.sqrt().range([3, 12]).domain(countExtent);
    
    // set initial x/y to values within the width/height
    // of the visualization
	data.nodes.forEach(function(n) {
      var randomnumber;
      n.x = randomnumber = Math.floor(Math.random() * width);
      n.y = randomnumber = Math.floor(Math.random() * height);
	  // add radius to the node so we can use it later
	  if (n.normalized_degree) return n.radius = (Math.log(100 * n.normalized_degree) *2);
	  else return n.radius = 6;
    });

	// ids -> node objects
    nodesMap = mapNodes(data.nodes);
	
	// switch links to point to node objects instead of ids
    data.links.forEach(function(l) {
      l.source = nodesMap.get(l.source);
      l.target = nodesMap.get(l.target);
	   // linkedByIndex is used for link sorting
      return linkedByIndex["" + l.source.id + "," + l.target.id] = 1;
    });
    return data;
  };

  // Helper function to map node ids to node objects.
  // Returns d3.map of ids -> nodes
  mapNodes = function(nodes) {
    var nodesMap;
    nodesMap = d3.map();
    nodes.forEach(function(n) {
      return nodesMap.set(n.id, n);
    });
    return nodesMap;
  };


	// Helper function that returns an associative array
  // with counts of unique attr in nodes
  // attr is value stored in node, like 'person'
  nodeCounts = function(nodes, attr) {
    var counts;
    counts = {};
    nodes.forEach(function(d) {
      var _name, _ref;
      if ((_ref = counts[_name = d[attr]]) == null) {
        counts[_name] = 0;
      }
      return counts[d[attr]] += 1;
    });
    return counts;
  };

  // Given two nodes a and b, returns true if
  // there is a link between them.
  // Uses linkedByIndex initialized in setupData
  neighboring = function(a, b) {
    return linkedByIndex[a.id + "," + b.id] || linkedByIndex[b.id + "," + a.id];
  };

  // Removes nodes from input array
  // based on current filter setting.
  // Returns array of nodes
  filterNodes = function(allNodes) {
    var cutoff, filteredNodes, playcounts;
    filteredNodes = allNodes;
    if (filter === "popular" || filter === "obscure") {
      playcounts = allNodes.map(function(d) {
      	if (d.normalized_degree)
        	return d.normalized_degree;
      }).sort(d3.ascending);
      cutoff = d3.quantile(playcounts, 0.5);
      filteredNodes = allNodes.filter(function(n) {
        if (filter === "popular") {
          return n.normalized_degree > cutoff;
        } else if (filter === "obscure") {
          return n.normalized_degree <= cutoff;
        }
      });
    }
    return filteredNodes;
  };

  // Returns array of nodes sorted based on
  // current sorting method.
    sortedCategories = function(nodes, links) {
    var occur, counts;
    occur = [];
   
   // Sorting by link property - docID
   if (sort === "links") {
      counts = {};
      links.forEach(function(l) {
        var _name, _name1, _ref, _ref1;
        if ((_ref = counts[_name = l.source.id]) == null) {
          counts[_name] = 0;
        }
        counts[l.docID] += 1;
        if ((_ref1 = counts[_name1 = l.target.id]) == null) {
          counts[_name1] = 0;
        }
        return counts[l] += 1;
      });

		 // add any missing node that dont have any links
      //nodes.forEach(function(n) {
      //  var _name, _ref;
      //  return (_ref = counts[_name = n.clan]) != null ? _ref : counts[_name] = 0;
     // });

		// sort based on counts
      occur = d3.entries(counts).sort(function(a, b) {
        return b.value - a.value;
      });

      // get just names
      occur = occur.map(function(v) {
        return v.key;
      });

    } else if (sort === "cluster") {
      counts = nodeCounts(nodes, "KmeansClusterGroup");
      occur = d3.entries(counts).sort(function(a, b) {
        return b.value - a.value;
      });
      occur = occur.map(function(v) {
        return v.key;
      });
    }
    else if (sort === "degree") {
      counts = nodeCounts(nodes, "normalized_degree");
      occur = d3.entries(counts).sort(function(a, b) {
        return b.value - a.value;
      });
      occur = occur.map(function(v) {
        return v.key;
      });
    }
    
    return occur;
  };



  updateCenters = function(occur) {
    if (layout === "radial") {
      return groupCenters = RadialPlacement().center({
        "x": width / 2,
        "y": height / 2 - 100
      }).radius(300).increment(18).keys(occur);
    }
  };


  // Removes links from allLinks whose
  // source or target is not present in curNodes
  // Returns array of links
  filterLinks = function(allLinks, curNodes) {
    curNodes = mapNodes(curNodes);
    return allLinks.filter(function(l) {
      return curNodes.get(l.source.id) && curNodes.get(l.target.id);
    });
  };

// enter/exit display for nodes
  updateNodes = function() {
    

	node = nodesG.selectAll("g.node").data(curNodesData, function(d) {
      return d.id;
    });
	
	
    node.enter().append("g")
	          .attr("class", "node")

	.append("circle").attr("class", "circle")
	.attr("r", function(d) {
      return d.radius;
    }).style("fill", function(d) {
      		return nodeColors(d.KmeansClusterGroup);
    }).style("stroke", function(d) {
      return strokeFor(d);
    }).style("stroke-width", 1.0)
	.call(node_drag);
	
	if (NodeTextTick){
	node.selectAll("text.text").remove();
	node.append("text").attr("class", "text")
	          .attr("x", function(d) { return 20; } )
	          .attr("y", function(d) {return -10;}  )
		  .attr("text-anchor", function(d) { return "start";} )
		  .attr("font-family", "Arial, Helvetica, sans-serif")
	          .style("font", "normal 11px Arial")
			  	.style("stroke-width", 0)
	          .attr("fill", function(d) {
		      return nodeColors(d.status);
		    	})
	          .attr("dy", ".25em")
	          .text(function(d) { return d.name; });
	}
	else {
		node.select("text.text").remove();
	}
	
    node.on("mouseover", showDetails).on("mouseout", hideDetails);
    return node.exit().remove();
  };

  var node_drag = d3.behavior.drag()
     .on("dragstart", dragstart)
     .on("drag", dragmove)
     .on("dragend", dragend);

  // enter/exit display for links
  updateLinks = function() {
    link = linksG.selectAll("path").data(curLinksData, function(d) {
      return "" + d.source.id + "_" + d.target.id;
    });
	if (VizEdges){
    link.enter()
	  .append("svg:path")
	   .attr("class", function(d) { return d.type; })
		.attr("fill", "none")
		.attr("d", function(d) {
		    var dx = d.target.x - d.source.x,
		        dy = d.target.y - d.source.y,
		        dr = Math.sqrt(dx * dx + dy * dy);
		    return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
		  })
		.attr("stroke", function(d) { return nodeColors(d.type); })
	    .attr("marker-end", function(d) { return "url(#" + d.directed + ")"; });
	   }
	   else{
	   	link.remove();
	   } 
	    
		link.on("mouseover", showEdgeDetails).on("mouseout", hideEdgeDetails);
		return link.exit().remove();
  };


	updateMarkers = function(){
		markers.selectAll();
		return markers.exit().remove();
	}
  // switches force to new layout parameters
  setLayout = function(newLayout) {
    layout = newLayout;
    if (layout === "force") {
      return force.on("tick", forceTick).charge(forceCharge).linkDistance(40);
    } else if (layout === "radial") {
      return force.on("tick", radialTick).charge(charge);
    }
  };

 // switches filter option to new filter
  setFilter = function(newFilter) {
    return filter = newFilter;
  };

 // switches sort option to new sort
  setSort = function(newSort) {
    return sort = newSort;
  };

  // tick function for force directed layout
  forceTick = function(e) {
    node.attr("transform", function(d) {
		var dx = Math.max(d.radius, Math.min(width - d.radius, d.x));
		var dy = Math.max(d.radius, Math.min(height - d.radius, d.y));
	    return "translate(" + dx + "," + dy + ")";
	  })
	//.attr("cx", function(d) { return d.x = Math.max(d.radius, Math.min(width - d.radius, d.x)); })
	//.attr("cy", function(d) { return d.y = Math.max(d.radius, Math.min(height - d.radius, d.y)); })
	.call(node_drag);
	
    return link.attr("d", function(d) {
	    var dx = d.target.x - d.source.x,
	        dy = d.target.y - d.source.y,
	        dr = Math.sqrt(dx * dx + dy * dy);
	    return "M" + d.source.x + "," + d.source.y + 
				"A" + dr + "," + dr + " 0 0,1 " + 
					d.target.x + "," + d.target.y;
	  });
  };


  // tick function for radial layout
  radialTick = function(e) {
    node.each(moveToRadialLayout(e.alpha));
    node.attr("transform", function(d) {
			var dx = Math.max(d.radius, Math.min(width - d.radius, d.x));
			var dy = Math.max(d.radius, Math.min(height - d.radius, d.y));
	    return "translate(" + dx + "," + dy + ")";
	  });
	  if (e.alpha < 0.03) {
      force.stop();
      return updateLinks();
    }
  };

  // Adjusts x/y for each node to
  // push them towards appropriate location.
  // Uses alpha to dampen effect over time.
  moveToRadialLayout = function(alpha) {
    var k;
    k = alpha * 0.1;
    return function(d) {
      var centerNode;
      if (sort === "degree") centerNode = groupCenters(d.normalized_degree);
	  else if (sort === "cluster") centerNode = groupCenters(d.KmeansClusterGroup);
	
      d.x += (centerNode.x - d.x) * k;
      return d.y += (centerNode.y - d.y) * k;
    };
  };

  // Helper function that returns stroke color for
  // particular node.
  strokeFor = function(d) {
    return d3.rgb(nodeColors(d.status)).darker().toString();
  };


// Helper function that returns stroke color for
  // particular node.
  fillFor = function(d) {
    return d3.rgb(nodeColors(d.KmeansClusterGroup)).darker().toString();
  };

  // Node mouseover tooltip function
  showDetails = function(d, i) {
    var content;
    content = '<p class="main"> <strong>' + "" + ' </strong></span></p>';
    if (d.name) content += '<p class="main"> <strong>' + d.name + ' </strong></span></p>';
    if (d.a1) content += '<p class="main"> <strong>' + d.a1 + ' </strong></span></p>';
    content += '<hr class="tooltip-hr">';

    if (d.father) content += '<p class="main"> <i> Father:</i> ' + d.father + '</span></p>';
	if (d.grandfather) content += '<p class="main"> <i> Grandfather:</i> ' + d.grandfather + '</span></p>';
	if (d.clan) content += '<p class="main"> <i> Clan:</i> ' + d.clan + '</span></p>';
    //if (d.status) content += '<p class="main"> <i>Status:</i> ' + d.status + '</span></p>';
    if (d.gender) content += '<p class="main"> <i>Gender:</i> ' + d.gender + '</span></p>';
    if (d.initials) content += '<p class="main"> <i>Initials:</i> ' + d.initials + '</span></p>';
	//insert more stuff here
	  content += '<hr class="tooltip-hr">';
    if (d.normalized_degree) content += '<p class="main"> <i> Normalized degree:</i> ' + d.normalized_degree + '</span></p>';
    if (d.degree) content += '<p class="main"> <i> Degree:</i> ' + d.degree + '</span></p>';
    if (d.BetweennessCentrality) content += '<p class="main"> <i> Betweenness centrality:</i> ' + d.BetweennessCentrality + '</span></p>';
    if (d.KmeansClusterGroup) content += '<p class="main"> <i> K-means cluster:</i> ' + d.KmeansClusterGroup + '</span></p>';
    if (d.normalized_degree) content += '<p class="main"> <i> Normalized degree:</i> ' + d.normalized_degree + '</span></p>';
	
	

    tooltip.showTooltip(content, d3.event);
    
    // higlight connected links
	if (link) {
      link.attr("stroke", function(l) {
        if (l.source === d || l.target === d) {
          return nodeColors(l.type);
        } else {
          return "#ddd";
        }
      }).attr("stroke-width", function(l) {
	        if (l.source === d || l.target === d) {
	          return 1.5;
	        } else {
	          return 1.0;
	        }
	      })
	.attr("stroke-opacity", function(l) {
        if (l.source === d || l.target === d) {
          return 1.0;
        } else {
          return 0.5;
        }
      });
    }
    // highlight neighboring nodes
    // watch out - don't mess with node if search is currently matching
    node.select("circle").style("stroke", function(n) {
      if (n.searched || neighboring(d, n)) {
        return "#555";
      } else {
        return strokeFor(n);
      }
	// highlight the node being moused over
    }).style("stroke-width", function(n) {
      if (n.searched || neighboring(d, n)) {
        return 3.0;
      } else {
        return 1.0;	
      }
    });
	
	node.select("text").style("stroke-width", 0.5);

    return d3.select(this).style("stroke", "black").style("stroke-width", 2.0);
  };

  // Mouseout function
  hideDetails = function(d, i) {
    tooltip.hideTooltip();
    node.select("circle").style("stroke", function(n) {
      if (!n.searched) {
        return strokeFor(n);
      } else {
        return "#555";
      }
    }).style("stroke-width", function(n) {
      if (!n.searched) {
        return 1.0;
      } else {
        return 2.0;
      }
    });

	node.select("text").style("stroke-width", 0);

    if (link) {
      return link
	.attr("stroke", function(d) { return nodeColors(d.type); })
	.attr("stroke-opacity", 1)
	.attr("stroke-width", 1);
    }
  };


 showEdgeDetails = function(d, i) {
    var content;
    content = '<p>From: <strong>' + d.source.name + ' </strong></span>';
	content += ' to: <strong>' + d.target.name + ' </strong></span></p>';
	if (d.type) content += '<h1 align ="center "class="main"  style="color:'+ nodeColors(d.type)+ '"> <strong>' + d.type + ' </strong></span></h1>';
    content += '<hr class="tooltip-hr">';
    if (d.docID) content += '<p class="main"> <i> DocID: </i> ' + d.docID + '</span></p>';
	if (d.weight) content += '<p class="main"> <i> Weight: </i> ' + d.weight + '</span></p>';
    if (d.directed) content += '<p class="main"> <i>Directed: </i> ' + d.directed + '</span></p>';
	//insert more stuff here

	
	
    tooltip.showTooltip(content, d3.event);

    return d3.select(this).style("stroke", function(l){nodeColors(l.type);}).style("stroke-width", 1.5);
  };

  // Mouseout function
  hideEdgeDetails = function(d, i) {
    tooltip.hideTooltip();
    
	d3.select(this).attr("stroke", function(d) { return nodeColors(d.type); })
	.style("stroke-opacity", 1)
	.style("stroke-width", 1);
    
  };


 nodeHood = function(d){
 	return d3.json("data.json?id=" + d.id, function(json) {return network.updateData(json); });
 }


  // Final act of Network() function is to return the inner 'network()' function.
  return network;


 function dragstart(d, i) {
    if (!paused){
	force.stop();
	}	// stops the force auto positioning before you start dragging
 }

 function dragmove(d, i) {
     d.px += d3.event.dx;
     d.py += d3.event.dy;
     d.x += d3.event.dx;
     d.y += d3.event.dy; 
     if (layout === "force") {
	       forceTick();
	    } else if (layout === "radial") {
	       radialTick();
	    }; // this is the key to make it work together with updating both px,py,x,y on d !
	    firstClick = true;
 }

 function dragend(d, i) {
     if (d.fixed && !firstClick)  {nodeHood(d); firstClick = true;}
	 else{firstClick = false;
     d.fixed = true;} // of course set the node to fixed so the force 
					//doesn't include the node in its auto positioning stuff
     update();
 }

};





// Activate selector button
activate = function(group, link) {
  d3.selectAll("#" + group + " a").classed("active", false);
  return d3.select("#" + group + " #" + link).classed("active", true);
};

//Execution entry point
$(function() {
  var myNetwork;
  myNetwork = Network();
  d3.selectAll("#layouts a").on("click", function(d) {
    var newLayout;
    newLayout = d3.select(this).attr("id");
    activate("layouts", newLayout);
    return myNetwork.toggleLayout(newLayout);
  });
  d3.selectAll("#filters a").on("click", function(d) {
    var newFilter;
    newFilter = d3.select(this).attr("id");
    activate("filters", newFilter);
    return myNetwork.toggleFilter(newFilter);
  });
  d3.selectAll("#sorts a").on("click", function(d) {
    var newSort;
    newSort = d3.select(this).attr("id");
    activate("sorts", newSort);
    return myNetwork.toggleSort(newSort);
  });
  $('#source_select').on("change", function(e) {
    var filename;
    filename = $(this).val();	
    return d3.json(filename, function(json) {
      return myNetwork.updateData(json);
    });
  });
  $('#force_slider').on("change", function(e) {
    return myNetwork.changeForce($(this).val());
  });
  d3.selectAll("#force a").on("click", function(d) {
    var force;
    force = d3.select(this).attr("id");
    activate("force", force);
    return myNetwork.toggleForce(force);
  });
  d3.selectAll("#nodenames a").on("click", function(d) {
    var names;
    names = d3.select(this).attr("id");
    activate("nodenames", names);
    return myNetwork.setNodeText(names);
  });
    d3.selectAll("#nodecolor a").on("click", function(d) {
    var names;
    names = d3.select(this).attr("id");
    activate("nodecolor", names);
    return myNetwork.setNodeColor(names);
  });
 d3.selectAll("#edgeshow a").on("click", function(d) {
    var names;
    names = d3.select(this).attr("id");
    activate("edgeshow", names);
    return myNetwork.setEdgesVisibile(names);
  });
 
  $("#search").keyup(function() {
    var searchTerm;
    searchTerm = $(this).val();
    return myNetwork.updateSearch(searchTerm);
  });
  return d3.json("/SNA/data.json", function(json) {
    return myNetwork("#vis", json);
  });
});
