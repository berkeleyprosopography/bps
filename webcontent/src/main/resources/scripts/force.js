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
  var allData, charge, curLinksData, curNodesData, filter, filterLinks, filterNodes, force, forceTick, groupCenters, height, hideDetails, layout, link, linkedByIndex, linksG, mapNodes, moveToRadialLayout, neighboring, network, node, nodeColors, nodeCounts, nodesG, radialTick, setFilter, setLayout, setSort, setupData, showDetails, sort, sortedArtists, strokeFor, tooltip, update, updateCenters, updateLinks, updateNodes, width;
  width = 800;
  height = 600;
  // allData will store the unfiltered data
  allData = [];
  curLinksData = [];
  curNodesData = [];
  linkedByIndex = {};
  // these will hold the svg groups for
  // accessing the nodes and links display
  nodesG = null;
  linksG = null;
  // these will point to the circles and lines
  // of the nodes and links
  node = null;
  link = null;
  // variables to reflect the current settings
  // of the visualization
  layout = "force";
  filter = "all";
  sort = "songs";
  // groupCenters will store our radial layout for
  // the group by artist layout.
  groupCenters = null;

  // our force directed layout
  force = d3.layout.force();
  // color function used to color nodes
  nodeColors = d3.scale.category20();
  // tooltip used to display details
  tooltip = Tooltip("vis-tooltip", 230);

  // charge used in artist layout
  charge = function(node) {
    return -Math.pow(node.radius, 2.0) / 2;
  };

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
    var artists;

	// filter data to show based on current filter settings.
    curNodesData = filterNodes(allData.nodes);
    curLinksData = filterLinks(allData.links, curNodesData);

	// sort nodes based on current sort and update centers for
    // radial layout
    if (layout === "radial") {
      artists = sortedArtists(curNodesData, curLinksData);
      updateCenters(artists);
    }

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

    // start me up!
    return force.start();
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
        element.style("fill", "#F38630").style("stroke-width", 2.0).style("stroke", "#555");
        return d.searched = true;
      } else {
        d.searched = false;
        return element.style("fill", function(d) {
          return nodeColors(d.artist);
        }).style("stroke-width", 1.0);
      }
    });
  };


  network.updateData = function(newData) {
    allData = setupData(newData);
    link.remove();
    node.remove();
    return update();
  };


  // called once to clean up raw data and switch links to
  // point to node instances
  // Returns modified data
  setupData = function(data) {
    var circleRadius, countExtent, nodesMap;
    countExtent = d3.extent(data.nodes, function(d) {
      return d.playcount;
    });
    circleRadius = d3.scale.sqrt().range([3, 12]).domain(countExtent);
    
    // set initial x/y to values within the width/height
    // of the visualization
	data.nodes.forEach(function(n) {
      var randomnumber;
      n.x = randomnumber = Math.floor(Math.random() * width);
      n.y = randomnumber = Math.floor(Math.random() * height);
	  // add radius to the node so we can use it later
      return n.radius = circleRadius(n.playcount);
    });

	// ids -> node objects
    nodesMap = mapNodes(data.nodes);
	
	// switch links to point to node objects instead of ids
    data.links.forEach(function(l) {
      l.source = nodesMap.get(l.source);
      l.target = nodesMap.get(l.target);
	   // linkedByIndex is used for link sorting
      linkedByIndex["" + l.source.id + "," + l.target.id] = 1;
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
  // attr is value stored in node, like 'artist'
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
        return d.playcount;
      }).sort(d3.ascending);
      cutoff = d3.quantile(playcounts, 0.5);
      filteredNodes = allNodes.filter(function(n) {
        if (filter === "popular") {
          return n.playcount > cutoff;
        } else if (filter === "obscure") {
          return n.playcount <= cutoff;
        }
      });
    }
    return filteredNodes;
  };

  // Returns array of artists sorted based on
  // current sorting method.
  sortedArtists = function(nodes, links) {
    var artists, counts;
    artists = [];
    if (sort === "links") {
      counts = {};
      links.forEach(function(l) {
        var _name, _name1, _ref, _ref1;
        if ((_ref = counts[_name = l.source.artist]) == null) {
          counts[_name] = 0;
        }
        counts[l.source.artist] += 1;
        if ((_ref1 = counts[_name1 = l.target.artist]) == null) {
          counts[_name1] = 0;
        }
        return counts[l.target.artist] += 1;
      });

		 // add any missing artists that dont have any links
      nodes.forEach(function(n) {
        var _name, _ref;
        return (_ref = counts[_name = n.artist]) != null ? _ref : counts[_name] = 0;
      });

		// sort based on counts
      artists = d3.entries(counts).sort(function(a, b) {
        return b.value - a.value;
      });

      // get just names
      artists = artists.map(function(v) {
        return v.key;
      });
    } else {
	      // sort artists by song count
      counts = nodeCounts(nodes, "artist");
      artists = d3.entries(counts).sort(function(a, b) {
        return b.value - a.value;
      });
      artists = artists.map(function(v) {
        return v.key;
      });
    }
    return artists;
  };



  updateCenters = function(artists) {
    if (layout === "radial") {
      return groupCenters = RadialPlacement().center({
        "x": width / 2,
        "y": height / 2 - 100
      }).radius(300).increment(18).keys(artists);
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
    node = nodesG.selectAll("circle.node").data(curNodesData, function(d) {
      return d.id;
    });
    node.enter().append("circle").attr("class", "node").attr("cx", function(d) {
      return d.x;
    }).attr("cy", function(d) {
      return d.y;
    }).attr("r", function(d) {
      return d.radius;
    }).style("fill", function(d) {
      return nodeColors(d.artist);
    }).style("stroke", function(d) {
      return strokeFor(d);
    }).style("stroke-width", 1.0);
    node.on("mouseover", showDetails).on("mouseout", hideDetails);
    return node.exit().remove();
  };

  // enter/exit display for links
  updateLinks = function() {
    link = linksG.selectAll("line.link").data(curLinksData, function(d) {
      return "" + d.source.id + "_" + d.target.id;
    });
    link.enter().append("line").attr("class", "link").attr("stroke", "#ddd").attr("stroke-opacity", 0.8).attr("x1", function(d) {
      return d.source.x;
    }).attr("y1", function(d) {
      return d.source.y;
    }).attr("x2", function(d) {
      return d.target.x;
    }).attr("y2", function(d) {
      return d.target.y;
    });
    return link.exit().remove();
  };

  // switches force to new layout parameters
  setLayout = function(newLayout) {
    layout = newLayout;
    if (layout === "force") {
      return force.on("tick", forceTick).charge(-200).linkDistance(50);
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
    node.attr("cx", function(d) {
      return d.x;
    }).attr("cy", function(d) {
      return d.y;
    });
    return link.attr("x1", function(d) {
      return d.source.x;
    }).attr("y1", function(d) {
      return d.source.y;
    }).attr("x2", function(d) {
      return d.target.x;
    }).attr("y2", function(d) {
      return d.target.y;
    });
  };


  // tick function for radial layout
  radialTick = function(e) {
    node.each(moveToRadialLayout(e.alpha));
    node.attr("cx", function(d) {
      return d.x;
    }).attr("cy", function(d) {
      return d.y;
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
      centerNode = groupCenters(d.artist);
      d.x += (centerNode.x - d.x) * k;
      return d.y += (centerNode.y - d.y) * k;
    };
  };

  // Helper function that returns stroke color for
  // particular node.
  strokeFor = function(d) {
    return d3.rgb(nodeColors(d.artist)).darker().toString();
  };

  // Mouseover tooltip function
  showDetails = function(d, i) {
    var content;
    content = '<p class="main">' + d.name + '</span></p>';
    content += '<hr class="tooltip-hr">';
    content += '<p class="main">' + d.artist + '</span></p>';
    tooltip.showTooltip(content, d3.event);
    
    // higlight connected links
	if (link) {
      link.attr("stroke", function(l) {
        if (l.source === d || l.target === d) {
          return "#555";
        } else {
          return "#ddd";
        }
      }).attr("stroke-opacity", function(l) {
        if (l.source === d || l.target === d) {
          return 1.0;
        } else {
          return 0.5;
        }
      });
    }
    // highlight neighboring nodes
    // watch out - don't mess with node if search is currently matching
    node.style("stroke", function(n) {
      if (n.searched || neighboring(d, n)) {
        return "#555";
      } else {
        return strokeFor(n);
      }
	// highlight the node being moused over
    }).style("stroke-width", function(n) {
      if (n.searched || neighboring(d, n)) {
        return 2.0;
      } else {
        return 1.0;
      }
    });
    return d3.select(this).style("stroke", "black").style("stroke-width", 2.0);
  };

  // Mouseout function
  hideDetails = function(d, i) {
    tooltip.hideTooltip();
    node.style("stroke", function(n) {
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
    if (link) {
      return link.attr("stroke", "#ddd").attr("stroke-opacity", 0.8);
    }
  };
  // Final act of Network() function is to return the inner 'network()' function.
  return network;
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
  $("#song_select").on("change", function(e) {
    var songFile;
    songFile = $(this).val();
    return d3.json("data/songs/" + songFile, function(json) {
      return myNetwork.updateData(json);
    });
  });
  $("#search").keyup(function() {
    var searchTerm;
    searchTerm = $(this).val();
    return myNetwork.updateSearch(searchTerm);
  });
  return d3.json("data/songs/call_me_al.json", function(json) {
    return myNetwork("#vis", json);
  });
});
