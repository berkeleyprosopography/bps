{include file="header.tpl"}

	<p class="nav-right"><a href="/corpora/corpus?id={$corpusID}">Return to Corpus details</a></p>

	<h1>Document Details</h1>
{if isset($errmsg) }
	<h2>{$errmsg}</h2>
{else}
	{if !isset($document) }
		<p>No document specified!</p>
	{else}
		<table class="docs_row" border="0" cellspacing="0" cellpadding="4px" width="100%">
			<tr>
				<td class="title" width="200px">Document</td>
				<td class="title" width="200px">Publication</td>
				<td class="title" width="400px">Notes</td>
				<td class="title" width="100px">Date</td>
				<td class="title" width="200px">XML ID</td>
			</tr>
			<tr>
				<td class="document" style="padding-top:6px">{$document.alt_id}</td>
				<td class="document" style="padding-top:6px">&nbsp; </td>
				<td class="document" style="padding-top:6px">{$document.notes}</td>
				<td class="document" style="padding-top:6px">{$document.date_str}</td>
				<td class="document" style="padding-top:6px">{$document.xml_id}</td>
			</tr>
		</table>
		<div class="nrads_row">
			{if empty($nrads)}
			<h2><i>No</i> Name-Role-Activity instances found in Document</h2>
			{else}
			<h2>{$nrads|@count} Name-Role-Activity instances in Document:</h2>
			<table class="nrads_row" border="0" cellspacing="0" cellpadding="4px" width="100%">
				<tr>
					<td class="title" width="200px">Name</td>
					<td class="title" width="200px">Normal Form</td>
					<td class="title" width="200px">Role</td>
					<td class="title" width="200px">Activity</td>
				</tr>
				{section name=nrad loop=$nrads}
					<tr>
						<td class="nrad" style="padding-top:6px">
							{$nrads[nrad].name}
						</td>
						<td class="nrad" style="padding-top:6px">
							{if $nrads[nrad].normalNameId != $nrads[nrad].nameId}
								{$nrads[nrad].normalName}
							{else}
							&nbsp;-&nbsp; 
							{/if}
						</td>
						<td class="nrad" style="padding-top:6px">
							{$nrads[nrad].activityRole}
						</td>
						<td class="nrad" style="padding-top:6px">
							{$nrads[nrad].activity}
						</td>
					</tr>
				{/section}
			</table>
			{/if}
		</div>
	{/if}
	<p>&nbsp;</p>
{/if}
	<p class="nav-left"><a href="/corpora/corpus?id={$corpusID}">Return to Corpus details</a></p>

{include file="footer.tpl"}
