{include file="header.tpl"}
{include file="workspace_header.tpl"}

{if !isset($workspace) }
	<h1>Error: No workspace specified!</h1>
{else}
	<h1>Administer My Workspace</h1>
	{if isset($errmsg) }
		<h2>{$errmsg}</h2>
	{else}
		<p>&nbsp;</p>
		<div class="form_row">
			<form class="form_row" method="post">
				<input type="hidden" name="id" value="{$workspace.id}" />
				<table class="form_row" border="0" cellspacing="0" cellpadding="4px" width="100%">
					<tr>
						 <td class="title 2" width="200px">Workspace Name</td>
						 <td class="title workspacendocs" width="80px"># Docs</td>
						 <td class="title" width="320px">Description</td>
						 <td class="title" width="100px">&nbsp;</td>
					</tr>
					<tr>
						<td class="workspace workspacename 2" width="200px">{$workspace.name}</td>
						<td class="workspace workspacendocs" width="80px">{$workspace.nDocs}</td>
						<td class="workspace workspacedesc 2" width="320px">
							<textarea id="D_{$workspace.id}" cols="40" rows="2"
								onkeyup="enableElement('U_{$workspace.id}',true);setStatusP('')"
								>{$workspace.description}</textarea>
						</td>
						<td class="corpus" width="100px">
							<input disabled id="U_{$workspace.id}" type="button"
								onclick="updateWorkspace('{$workspace.id}','{$workspace.name}')" value=" Update " />
						</td>
					</tr>
				</table>
			</form>
		</div>
		<p>&nbsp;</p>
		<div class="docs_row">
			{if empty($workspace.importedCorpusName)}
				<h2><i>No</i> Corpus (yet) imported into Workspace</h2>
				{if isset($corpora)}
					<p>Select a corpus to import into your workspace.</p>
					<p>&nbsp;</p>
						<table border="0" cellspacing="0" cellpadding="5px">
							<tr>
								 <td class="title 2" width="200px">Corpus Name</td>
								 <td class="title corpusndocs" width="80px"># Docs</td>
								 <td class="title" width="320px">Description</td>
							</tr>
						</table>
					{section name=corpus loop=$corpora}
						<div class="form_row">
						<form class="form_row" method="post">
							<input type="hidden" name="id" value="{$corpora[corpus].id}" />
							<table class="form_row" border="0" cellspacing="0" cellpadding="4px">
								<tr>
									<td class="corpus corpusname 2" width="200px">{$corpora[corpus].name}</td>
									<td class="corpus corpusndocs" width="80px">{$corpora[corpus].nDocs}</td>
									<td class="corpus corpusdesc 2" width="320px">{$corpora[corpus].description}</td>
									<td class="corpus" width="100px">
										<input id="importCorpButton_{$corpora[corpus].id}" type="button" value="Import Corpus"
													onclick="workspaceSetCorpus({$workspace.id}, {$corpora[corpus].id},false)" />
									</td>
								</tr>
							</table>
						</form>
						</div>
					{/section}
					<p id="buildingP"></p>
				{/if}
			{else}
			<table>
				<tr>
					<td>
						<h2>Corpus: "{$workspace.importedCorpusName}" imported into Workspace</h2>
					</td>
					<td width="20px">
					</td>
					<td>
						<input id="importCorpButton_{$workspace.importedCorpusId}" type="button" value="Rebuild from Corpus"
									onclick="workspaceSetCorpus({$workspace.id}, {$workspace.importedCorpusId},true)" />
						&nbsp; &nbsp;
						<input id="rebuildEntitiesButton" type="button" value="Rebuild Entities"
									onclick="workspaceRebuildEntities({$workspace.id})" />
					</td>
				</tr>
			</table>
			<p id="buildingP"></p>
		</div>
		{/if}
{/if}
	<p>&nbsp;</p>
	{if isset($opmsg) }
		<p id="statusP">{$opmsg}</p>
	{else}
		<p id="statusP"> &nbsp; </p>
	{/if}
{/if}
{include file="footer.tpl"}
