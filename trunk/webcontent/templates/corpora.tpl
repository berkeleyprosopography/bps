{include file="header.tpl"}

	<h1>Corpora Management</h1>
{if isset($corpora_error) }
	<h2>{$corpora_error}</h2>
{else}
	{if !isset($corpora) }
		<p>There are no corpora defined in the system.</p>
	{else}
		<p>Click on a corpus name for details, and to import.</p>
		<p>&nbsp;</p>
			<table border="0" cellspacing="0" cellpadding="5px">
				<tr>
					 <td class="title 2" width="200px">Corpus Name</td>
					 <td class="title" width="320px">Description</td>
					 <td class="title corpusndocs" width="80px"># Docs</td>
					{if isset($canDeleteCorpus) }
					 <td class="title" width="100px">&nbsp;</td>
					{/if}
				</tr>
			</table>
		{section name=corpus loop=$corpora}
			<div class="form_row">
			<form class="form_row" method="post">
				<input type="hidden" name="id" value="{$corpora[corpus].id}" />
				<table class="form_row" border="0" cellspacing="0" cellpadding="4px">
					<tr>
						<td class="corpus corpusname 2" width="200px">
						  <a href="corpus?id={$corpora[corpus].id}">{$corpora[corpus].name}</a>
						</td>
						<td class="corpus corpusdesc 2" width="320px">
							{$corpora[corpus].description}
						</td>
						<td class="corpus corpusndocs" width="80px">{$corpora[corpus].nDocs}</td>
					{if isset($canDeleteCorpus) }
						<td class="corpus" width="100px">
							<input id="deleteCorpButton_{$corpora[corpus].id}" type="button" value="Delete Corpus"
										onclick="deleteCorpus({$corpora[corpus].id})" />
						</td>
					{/if}
					</tr>
				</table>
			</form>
			</div>
		{/section}
	{/if}
	<div style="height:20px"></div>
	{if isset($canAddCorpus) }
	<div class="form_row">
	<form method="post" onsubmit="checkValues(event, this.corpname, this.desc, 255);">
		<table class="form_row" order="0" cellspacing="0" cellpadding="4px">
			<tr>
				<td class="title" colspan="3">Add a New Corpus</td>
			</tr>
			<tr height="8px"></tr>
				<tr>
					 <td class="corpus" width="200px">Corpus Name</td>
					 <td class="corpus" width="320px">Description</td>
					 <td class="corpus" width="80px">&nbsp;</td>
				</tr>
			<tr>
				<td class="2" width="200px"><input type="text" id="newCorpusName" name="corpname" maxlength="40"></td>
				<td class="corpusdesc" width="320px"><textarea id="newCorpusDesc" name="desc" rows="2" cols="40" 
				       onkeypress="limitChars(this,255);"
							 onblur="limitChars(this,255);"
							 onfocus="limitChars(this,255);"></textarea></td>
				<td class="corpus" width="80px">
					<input id="addCorpButton" type="button" onclick="addCorpus()" value=" Add " />
				</td>
			</tr>
		</table>
	</form>
	</div>
	{/if}
	{if isset($opmsg) }
		<p>&nbsp;</p>
		<p>{$opmsg}</p>
	{/if}
{/if}
{include file="footer.tpl"}
