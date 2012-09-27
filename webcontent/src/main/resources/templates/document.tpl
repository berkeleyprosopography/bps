{include file="header.tpl"}

	<p class="nav-right">
	{if isset($corpusID) }
		<a href="/corpora/corpus?id={$corpusID}">Return to Corpus details</a>
		</p>
		<h1>Corpus Document Details</h1>
	{elseif isset($workspaceID) }
		<a href="/workspace?id={$workspaceID}">Return to Workspace details</a>
		</p>
		<h1>Workspace Document Details</h1>
	{/if}

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
			</tr>
			<tr>
				<td class="document" style="padding-top:6px">{$document.alt_id}</td>
				<td class="document" style="padding-top:6px">{$document.primaryPubl}</td>
				<td class="document" style="padding-top:6px">{$document.notes}</td>
				<td class="document" style="padding-top:6px">{$document.date_str}</td>
			</tr>
		</table>
		<p>&nbsp;&nbsp;See also:
				<a href="http://cdli.ucla.edu/{$document.alt_id}">CDLI</a>
				&nbsp;&nbsp;<a href="http://oracc.museum.upenn.edu/hbtin/{$document.alt_id}" target="_blank">Oracc</a>
				&nbsp;&nbsp;<a href="http://oracc.museum.upenn.edu/hbtin/{$document.alt_id}/tei" target="_blank">TEI</a>
				&nbsp;&nbsp;<a href="http://cdli.ucla.edu/dl/photo/{$document.alt_id}.jpg" target="_blank">Image</a>
				&nbsp;&nbsp;<a href="http://cdli.ucla.edu/dl/lineart/{$document.alt_id}_l.jpg" target="_blank">Line art</a>
		</p>
		<div class="nrads_row">
			{if empty($nrads)}
			<h2><i>No</i> Name-Role-Activity instances found in Document</h2>
			{else}
			<h2>{$nrads|@count} Name-Role-Activity instances in Document:</h2>
			<table class="nrads_row" border="0" cellspacing="0" cellpadding="4px" width="100%">
				<tr>
					<td class="title" width="200px">Name</td>
					<td class="title" width="200px">Normalized Form</td>
					<td class="title" width="140px">Role</td>
					<td class="title" width="140px">Activity</td>
					<td class="title" width="140px">XML ID</td>
				</tr>
				{section name=nrad loop=$nrads}
					<tr>
						<td class="nrad" style="padding-top:6px">
							{if $nrads[nrad].activityRoleIsFamily}
								<span class="familyIndent">&nbsp;</span>
							{/if}
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
							{if $nrads[nrad].activityRoleIsFamily}
								<span class="familyIndent">&nbsp;</span>
							{/if}
							{$nrads[nrad].activityRole}
						</td>
						<td class="nrad" style="padding-top:6px">
							{$nrads[nrad].activity}
						</td>
						<td class="nrad" style="padding-top:6px">
							{$nrads[nrad].xmlId}
						</td>
					</tr>
						{if isset($nrads[nrad].links)}
							{section name=ilink loop=$nrads[nrad].links}
								{if $nrads[nrad].links[ilink].weight > 0 }
									<tr>
										<td class="nrad" colspan="3">
											{if $nrads[nrad].activityRoleIsFamily}
												<span class="familyIndent">&nbsp;</span>
											{/if}
											<span class="nradLink">-- {$nrads[nrad].links[ilink].linkTo}&nbsp;
												<span class="nradLinkWeight">({$nrads[nrad].links[ilink].weight}%)</span>
											</span>
										</td>
									</tr>
								{/if}
							{/section}
						{elseif isset($workspaceID)}
							<tr>
								<td class="nrad" colspan="3">
									{if $nrads[nrad].activityRoleIsFamily}
										<span class="familyIndent">&nbsp;</span>
									{/if}
									<span class="nradLink">-- (no Person/Clan associations computed)</span>
								</td>
							</tr>
						{/if}
				{/section}
			</table>
			{/if}
		</div>
	{/if}
	<p>&nbsp;</p>
{/if}
	<p class="nav-right">
	{if isset($corpusID) }
		<a href="/corpora/corpus?id={$corpusID}">Return to Corpus details</a>
	{elseif isset($workspaceID) }
		<a href="/workspace?id={$workspaceID}">Return to Workspace details</a>
	{/if}
	</p>

{include file="footer.tpl"}
