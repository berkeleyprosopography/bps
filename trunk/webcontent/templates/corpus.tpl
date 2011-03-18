{include file="header.tpl"}

	<h1>Corpus Details</h1>
{if isset($errmsg) }
	<h2>{$errmsg}</h2>
{else}
	{if !isset($corpus) }
		<p>No corpus specified!</p>
	{else}
		<p>&nbsp;</p>
		<table border="0" cellspacing="0" cellpadding="5px" width="100%">
			<tr>
				 <td class="title 2" width="200px">Corpus Name</td>
				 <td class="title corpusndocs" width="80px"># Docs</td>
				 <td class="title" width="320px">Description</td>
				{if isset($canUpdateCorpus) }
				 <td class="title" width="100px">&nbsp;</td>
				{/if}
			</tr>
		</table>
		<div class="form_row">
			<form class="form_row" method="post">
				<input type="hidden" name="id" value="{$corpus.id}" />
				<table class="form_row" border="0" cellspacing="0" cellpadding="4px" width="100%">
					<tr>
						<td class="corpus corpusname 2" width="200px">{$corpus.name}</td>
						<td class="corpus corpusndocs" width="80px">{$corpus.nDocs}</td>
					{if !isset($canUpdateCorpus) }
						<td class="corpus corpusdesc 2" width="320px">
							{$corpus.description}
						</td>
					{else}
						<td class="corpus corpusdesc 2" width="320px">
							<textarea id="D_{$corpus.id}" cols="40" rows="2"
								onkeyup="enableElement('U_{$corpus.id}',true);setStatusP('')"
								>{$corpus.description}</textarea>
						</td>
						<td class="corpus" width="100px">
							<input disabled id="U_{$corpus.id}" type="button"
								onclick="updateCorpus('{$corpus.id}','{$corpus.name}')" value=" Update " />
						</td>
					{/if}
					</tr>
				</table>
			</form>
		</div>
		<p>&nbsp;</p>
		{if isset($corpus_file) }
		<p><strong>
			Corpus file has been uploaded. 
			  <a href="{$teiloc}">View tei</a>&nbsp;&nbsp;
			  <a href="{$teisummaryloc}">Validate and view summary.</a>&nbsp;&nbsp;
			</strong>
			{if isset($canUpdateCorpus) }
				<input id="processTEIBtn" type="button"
								onclick="processTEI('{$corpus.id}')" value="  Rebuild corpus from TEI  " />
			{/if}
		</p>
		{/if}
		{if isset($canUpdateCorpus) }
		<div class="form_row">
			<form enctype="multipart/form-data" action="../../api/uploadTEI.php" method="POST">
				<!-- MAX_FILE_SIZE must precede the file input field -->
				<input type="hidden" name="id" value="{$corpus.id}" />
				<input type="hidden" name="MAX_FILE_SIZE" value="{$maxfilesizeTEI}" />
				<!-- Name of input element determines name in $_FILES array -->
				<table class="form_row" border="0" cellspacing="0" cellpadding="4px">
					<tr height="40px">
						<td class="corpus corpusname 2" width="200px">
						   Upload a {if isset($corpus_file)}<i>new</i> {/if}TEI Corpus file:</td>
						<td colspan="2" class="corpus corpusdesc 2" >
							<input name="teifile" type="file" size="44" />
							&nbsp;<input type="submit" value="Upload File" />
						</td>
					</tr>
				</table>
			</form>
			{if isset($dates_file) }
			<p><strong>
				Corpus Dates file has been uploaded. 
				</strong>
					<input id="processDatesBtn" type="button"
									onclick="processDates('{$corpus.id}')" 
									value="  Process Dates assertions  " />
			</p>
			{/if}
			<form enctype="multipart/form-data" action="../../api/uploadDatesAssertions.php" method="POST">
				<!-- MAX_FILE_SIZE must precede the file input field -->
				<input type="hidden" name="id" value="{$corpus.id}" />
				<input type="hidden" name="MAX_FILE_SIZE" value="{$maxfilesizeAssertions}" />
				<!-- Name of input element determines name in $_FILES array -->
				<table class="form_row" border="0" cellspacing="0" cellpadding="4px">
					<tr height="40px">
						<td class="corpus corpusname 2" width="200px">
						   Upload a {if isset($dates_file)}<i>new</i> {/if}Corpus Dates file:</td>
						<td colspan="2" class="corpus corpusdesc 2" >
							<input name="datesfile" type="file" size="44" />
							&nbsp;<input type="submit" value="Upload File" />
						</td>
					</tr>
				</table>
			</form>
		</div>
		{/if}
			<div class="docs_row">
				{if empty($documents)}
				<h2><i>No</i> Documents (yet) in Corpus</h2>
				{else}
				<h2>{$documents|@count} Documents in Corpus:</h2>
				<table class="docs_row" border="0" cellspacing="0" cellpadding="4px" width="100%">
					<tr>
						<td class="title" width="200px">Document</td>
						<td class="title" width="200px">Publication</td>
						<td class="title" width="400px">Notes</td>
						<td class="title" width="100px">Date</td>
						<td class="title" width="200px">XML ID</td>
					</tr>
					{section name=doc loop=$documents}
						<tr>
							<td class="document" style="padding-top:6px">
								<a href="/document?cid={$corpus.id}&did={$documents[doc].id}">{$documents[doc].alt_id}</a></td>
							<td class="document" style="padding-top:6px">&nbsp; </td>
							<td class="document" style="padding-top:6px">{$documents[doc].notes}</td>
							<td class="document" style="padding-top:6px">{$documents[doc].date_str}</td>
							<td class="document" style="padding-top:6px">{$documents[doc].xml_id}</td>
						</tr>
					{/section}
				</table>
				{/if}
			</div>
	{/if}
	<p>&nbsp;</p>
	{if isset($opmsg) }
		<p id="statusP">{$opmsg}</p>
	{else}
		<p id="statusP"> &nbsp; </p>
	{/if}
{/if}
{include file="footer.tpl"}
