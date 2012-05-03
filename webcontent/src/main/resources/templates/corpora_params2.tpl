{include file="header.tpl"}

<div id="contentNarrow">
<br />
<h1>General settings:</h1>

<table cellpadding="10px" class="params">
  <tr><td class="paramDesc"><i><b>Number of qualifications (father/grandfather/ancestor/clan) in
      addition to forename required to consider a name citation &quot;fully
      qualified&quot;</b></i></td>
    <td width="150px" class="paramDesc"><input type="text" value="2" size="5" style="text-align:right;"  class="bpsFormInput"/></td>
  </tr>
  <tr><td class="paramDesc"><b><i>Assumed typical length of active business life (years)</i></b></td>
    <td width="150px" class="paramDesc"><input type="text" value="25" size="5" style="text-align:right;"  class="bpsFormInput"/></td>
  </tr>
  <tr><td class="paramDesc"><b><i>Assumed typical length of whole life (years)</i></b></td>
    <td width="150px" class="paramDesc"><input type="text" value="40" size="5" style="text-align:right;"  class="bpsFormInput"/></td>
  </tr>
</table>

<h1>Step 1: Intra-document rules:</h1>


<h3><i>Rule Steps 1A, 1B, and 1C collapse citations within a single document.</i></h3>

<h2>Step 1A: Consider primary actors/cited persons (usually, sons or
daughters)</h2>


<table cellpadding="10px" class="params">
  <tr><td colspan="3" class="caseTitle">
      <h3><b>Case: Collapse fully qualified citations with same qualifiers&nbsp;<br>
      </b> (e.g., &quot;<b><i> A son-of B in-clan C</i></b>&quot;<b><i></i> </b> and<b>
      </b>&quot;<b><i> A son-of B in-clan C</i></b>&quot;<b><i></i></b>)</h3>
    </td>
  </tr>
  <tr>
    <td class="paramDesc" ></td>
    <td class="paramDesc" >Role 2: Principle</td>
    <td class="paramDesc" >Role 2: Witness</td>
  </tr>
  <tr>
    <td class="paramDesc" >Role 1: Principle</td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
  <tr class="paramDesc lastParamRow">
    <td class="paramDesc" >Role 1: Witness</td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
  <tr><td colspan="3" class="caseTitle">
      <h3><b>Case: Collapse partly qualified citations with same qualifiers</b> &nbsp;<br>
 (e.g.,
      &quot;<b><i> A son-of B</i></b>&quot;<b><i></i></b> and &quot;<b><i> A son-of
      B</i></b>&quot;<b><i></i></b>)</h3>
    </td>
  </tr>
  <tr>
    <td class="paramDesc" ></td>
    <td class="paramDesc" >Role 2: Principle</td>
    <td class="paramDesc" >Role 2: Witness</td>
  </tr>
  <tr>
    <td class="paramDesc" >Role 1: Principle</td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
  <tr class="paramDesc lastParamRow">
    <td class="paramDesc" >Role 1: Witness</td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
  <tr><td colspan="3" class="caseTitle" >
      <h3><b>Case: Collapse partly qualified
      citation into more qualified citation</b> &nbsp;<br>
 (e.g., &quot;<b><i>A son-of B</i></b>&quot;
      and &quot;<b><i>A son-of B in-clan C</i></b>&quot;)</h3>
    </td>
  </tr>
  <tr>
    <td class="paramDesc" ></td>
    <td class="paramDesc" >Role 2: Principle</td>
    <td class="paramDesc" >Role 2: Witness</td>
  </tr>
  <tr>
    <td class="paramDesc" >Role 1: Principle</td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
  <tr class="paramDesc lastParamRow">
    <td class="paramDesc" >Role 1: Witness</td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
  <tr><td colspan="3" class="caseTitle" >
      <h3><b>Case: Collapse unqualified citations
      into more qualified citations</b> &nbsp;<br>
 (e.g.,<b> </b>&quot;<b><i>A</i></b>&quot;
      and &quot;<b><i>A son-of B</i></b>&quot;, OR &quot;<b><i>A</i></b>&quot;
      and &quot;<b><i>A son-of B in-clan C</i></b>&quot;)</h3>
    </td>
  </tr>
  <tr>
    <td class="paramDesc" ></td>
    <td class="paramDesc" >Role 2: Principle</td>
    <td class="paramDesc" >Role 2: Witness</td>
  </tr>
  <tr>
    <td class="paramDesc" >Role 1: Principle</td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
  <tr class="paramDesc lastParamRow">
    <td class="paramDesc" >Role 1: Witness</td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
</table>

<h2>Step 1B: Consider cited fathers/mothers of actors/cited actors, by role
of son/daughter</h2>


<p><i>Question: is it meaningful to consider the role of the actor son/daughter,
or does this not really matter once we're talking about parents and
grandparents?</i></p>


<table cellpadding="10px" class="params">
  <tr><td colspan="3" class="caseTitle">
      <h3><b>Case: Collapse fully qualified citations with same qualifiers&nbsp;<br>
      </b> (e.g., &quot;<b><i>A, father-of (any actor), son-of B in-clan C</i></b>&quot;<b><i></i>
      </b> and<b> </b>&quot;<b><i>A, father-of (any actor), son-of B in-clan C</i></b>&quot;<b><i></i></b>)</h3>
    </td>
  </tr>
  <tr>
    <td class="paramDesc"></td>
    <td class="paramDesc">Role 2: Principle</td>
    <td class="paramDesc">Role 2: Witness</td>
  </tr>
  <tr>
    <td class="paramDesc">Role 1: Principle</td>
    <td class="paramDesc">
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc">
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
  <tr class="paramDesc lastParamRow">
    <td class="paramDesc" >Role 1: Witness</td>
    <td class="paramDesc">
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc">
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
  <tr><td colspan="3" class="caseTitle">
      <h3><b>Case: Collapse partly qualified citations with same qualifiers</b> &nbsp;<br>
 (e.g.,
      &quot;<b><i>A, father-of (any actor), son-of C</i></b>&quot;<b><i></i></b> and
      &quot;<b><i>A, father-of (any actor), son-of C</i></b>&quot;<b><i></i></b>)</h3>
    </td>
  </tr>
  <tr>
    <td class="paramDesc"></td>
    <td class="paramDesc">Role 2: Principle</td>
    <td class="paramDesc">Role 2: Witness</td>
  </tr>
  <tr>
    <td class="paramDesc">Role 1: Principle</td>
    <td class="paramDesc">
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc">
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
  <tr class="paramDesc lastParamRow">
    <td class="paramDesc" >Role 1: Witness</td>
    <td class="paramDesc">
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc">
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
  <tr><td colspan="3" class="caseTitle">
      <h3><b>Case: Collapse partly qualified
      citation into more qualified citation</b> &nbsp;<br>
 (e.g., &quot;<b><i>A, father-of (any actor), son-of B</i></b>&quot;
      and &quot;<b><i>A, father-of (any actor), son-of B in-clan C</i></b>&quot;)</h3>
    </td>
  </tr>
  <tr>
    <td class="paramDesc"></td>
    <td class="paramDesc">Role 2: Principle</td>
    <td class="paramDesc">Role 2: Witness</td>
  </tr>
  <tr>
    <td class="paramDesc">Role 1: Principle</td>
    <td class="paramDesc">
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc">
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
  <tr class="paramDesc lastParamRow">
    <td class="paramDesc" >Role 1: Witness</td>
    <td class="paramDesc">
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc">
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
  <tr><td colspan="3" class="caseTitle">
      <h3><b>Case: Collapse unqualified citations
      into more qualified citations</b> &nbsp;<br>
 (e.g.,<b> </b>&quot;<b><i>A, father-of (any
      actor)</i></b>&quot; and &quot;<b><i>A, father-of (any actor), in-clan C</i></b>&quot;)</h3>
    </td>
  </tr>
  <tr>
    <td class="paramDesc"></td>
    <td class="paramDesc">Role 2: Principle</td>
    <td class="paramDesc">Role 2: Witness</td>
  </tr>
  <tr>
    <td class="paramDesc">Role 1: Principle</td>
    <td class="paramDesc">
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc">
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
  <tr class="paramDesc lastParamRow">
    <td class="paramDesc" >Role 1: Witness</td>
    <td class="paramDesc">
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc">
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
</table>

<h2>Step 1C: Consider cited grandfathers/grandmothers of actors/cited actors,
by role of grandson/granddaughter</h2>


<p><i>Question: is it meaningful to consider the role of the actor </i>grand<i>son/</i>grand<i>daughter,
or does this not really matter once we're talking about grandparents?</i></p>


<table cellpadding="10px" class="params" >
  <tr><td colspan="3" class="caseTitle" >
      <h3><b>Case: Collapse fully qualified citations with same qualifiers&nbsp;<br>
      </b> (e.g., &quot;<b><i>A, grandfather-of (any actor), son-of B, in-clan C</i></b>&quot;<b><i></i>
      </b> and<b> </b>&quot;<b><i>A, grandfather-of (any actor), son-of B, in-clan
      C</i></b>&quot;<b><i></i></b>).<b> </b><i>This is rare and unlikely.</i></h3>
    </td>
  </tr>
  <tr>
    <td class="paramDesc" ></td>
    <td class="paramDesc" >Role 2: Principle</td>
    <td class="paramDesc" >Role 2: Witness</td>
  </tr>
  <tr>
    <td class="paramDesc" >Role 1: Principle</td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
  <tr class="paramDesc lastParamRow">
    <td class="paramDesc" >Role 1: Witness</td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
  <tr><td colspan="3" class="caseTitle" >
      <h3><b>Case: Collapse partly qualified citations with same qualifiers</b> &nbsp;<br>
 (e.g.,
      &quot;<b><i>A, grandfather-of (any actor), in-clan C</i></b>&quot;<b><i></i></b> and
      &quot;<b><i>A, grandfather-of (any actor), in-clan C</i></b>&quot;<b><i></i></b>)</h3>
    </td>
  </tr>
  <tr>
    <td class="paramDesc" ></td>
    <td class="paramDesc" >Role 2: Principle</td>
    <td class="paramDesc" >Role 2: Witness</td>
  </tr>
  <tr>
    <td class="paramDesc" >Role 1: Principle</td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
  <tr class="paramDesc lastParamRow">
    <td class="paramDesc" >Role 1: Witness</td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
  <tr><td colspan="3" class="caseTitle" >
      <h3><b>Case: Collapse partly qualified
      citation into more qualified citation</b> &nbsp;<br>
 (e.g., &quot;<b><i>A, grandfather-of (any actor), son-of B</i></b>&quot;
      and &quot;<b><i>A, grandfather-of (any actor), son-of B, in-clan C</i></b>&quot;)</h3>
    </td>
  </tr>
  <tr>
    <td class="paramDesc" ></td>
    <td class="paramDesc" >Role 2: Principle</td>
    <td class="paramDesc" >Role 2: Witness</td>
  </tr>
  <tr>
    <td class="paramDesc" >Role 1: Principle</td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
  <tr class="paramDesc lastParamRow">
    <td class="paramDesc" >Role 1: Witness</td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
  <tr><td colspan="3" class="caseTitle" >
      <h3><b>Case: Collapse unqualified citations
      into more qualified citations</b> &nbsp;<br>
 (e.g.,<b> </b>&quot;<b><i>A, grandfather-of (any actor)</i></b>&quot;
      and &quot;<b><i>A, grandfather-of (any actor), in-clan C</i></b>&quot;)</h3>
    </td>
  </tr>
  <tr>
    <td class="paramDesc" ></td>
    <td class="paramDesc" >Role 2: Principle</td>
    <td class="paramDesc" >Role 2: Witness</td>
  </tr>
  <tr>
    <td class="paramDesc" >Role 1: Principle</td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
  <tr class="paramDesc lastParamRow">
    <td class="paramDesc" >Role 1: Witness</td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
    <td class="paramDesc" >
      <select class="bpsFormInput">
       <option selected="1">Always: 100%</option>
       <option>Aggressive: 75%</option>
       <option>Moderate: 50%</option>
       <option>Conservative: 30%</option>
       <option>Ignore/No: 0%</option>
      </select>
    </td>
  </tr>
</table>

<p>&nbsp;</p>

<p>&nbsp;</p>

<h1>Step 2: Inter-document rules:</h1>

<table cellpadding="10px" class="params">
  <tr><td colspan="2" class="paramDesc"><h3><i>Step 2 rules collapse citations across
      (between/among) documents within a corpus.</i></h3></td>
  </tr>
  <tr><td class="paramDesc"><b>Collapse fully qualified and equal references across documents.</b></td>
    <td width="150px" class="paramDesc"><select class="bpsFormInput">
      <option selected="1">Always: 100%</option>
      <option>Aggressive: 75%</option>
      <option>Moderate: 50%</option>
      <option>Conservative: 30%</option>
      <option>Ignore/No: 0%</option>
    </select></td>
  </tr>
  <tr><td class="paramDesc"><b>Collapse simple references into more qualified names in other
    documents.</b></td>
    <td width="150px" class="paramDesc"><select class="bpsFormInput">
      <option>Always: 100%</option>
      <option>Aggressive: 75%</option>
      <option>Moderate: 50%</option>
      <option selected="1">Conservative: 30%</option>
      <option>Ignore/No: 0%</option>
    </select>
    </td>
  </tr>
  <tr><td class="paramDesc"><b>Collapse less qualified references into more qualified names in
    other documents.</b></td>
    <td width="150px" class="paramDesc"><select class="bpsFormInput">
      <option>Always: 100%</option>
      <option>Aggressive: 75%</option>
      <option selected="1">Moderate: 50%</option>
      <option>Conservative: 30%</option>
      <option>Ignore/No: 0%</option>
    </select>
    </td>
  </tr>
</table>


</div>


{include file="footer.tpl"}