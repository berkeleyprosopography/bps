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

<h1>Intra-document rules:</h1>


<table cellpadding="10px" class="params">
  <tr><td colspan="2" class="paramDesc"><h3><i>These rules are applied first, to collapse Persons within a single document.</i></h3></td>
  </tr>
  <tr><td class="paramDesc"><b>Collapse partially or fully qualified Persons with same generational
    rank in the document, with equal references, in the same document, with
    compatible roles for the generational rank.
    </b>
      <p class="paramNote">We consider the generationalRank in considering the roles, since the
    primary persons cannot collapse across certain roles, but fathers often can.</p>
    </td>
    <td width="150px" class="paramDesc"><select class="bpsFormInput">
      <option selected="1">Always: 100%</option>
      <option>Aggressive: 75%</option>
      <option>Moderate: 50%</option>
      <option>Conservative: 30%</option>
      <option>Ignore/No: 0%</option>
    </select></td>
  </tr>
  <tr><td class="paramDesc"><b>Collapse simple references into more qualified references in the same
    document, with the same generational rank in the document, and with a
    compatible role.</b></td>
    <td width="150px" class="paramDesc"><select class="bpsFormInput">
      <option>Always: 100%</option>
      <option selected="1">Aggressive: 75%</option>
      <option>Moderate: 50%</option>
      <option>Conservative: 30%</option>
      <option>Ignore/No: 0%</option>
    </select>
    </td>
  </tr>
  <tr><td class="paramDesc"><b>Collapse less qualified references into more qualified references in the
    same document, with the same generational rank in the document, and with a
    compatible role.</b></td>
    <td width="150px" class="paramDesc"><select class="bpsFormInput">
      <option>Always: 100%</option>
      <option>Aggressive: 75%</option>
      <option>Moderate: 50%</option>
      <option>Conservative: 30%</option>
      <option selected="1">Ignore/No: 0%</option>
    </select>
    </td>
  </tr>
</table>

<h1>Inter-document rules:</h1>

<table cellpadding="10px" class="params">
  <tr><td colspan="2" class="paramDesc"><h3><i>These rules are applied next, to collapse Persons across
      documents within a corpus.</i></h3></td>
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
