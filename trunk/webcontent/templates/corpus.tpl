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
								onkeyup="enableElement('U_{$corpus.id}');setStatusP('')"
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
		{if !empty($documents)}
			<div class="docs_row">
				<h2>Documents in Corpus</h2>
				<table class="docs_row" border="0" cellspacing="0" cellpadding="4px" width="100%">
					<tr>
						<td class="title" width="200px">Alt. ID</td>
					</tr>
					{section name=doc loop=$documents}
						<tr>
							<td class="document" style="padding-top:6px" width="200px">
								{$documents[doc].alt_id}
							</td>
						</tr>
					{/section}
				</table>
			</div>
		{/if}
		<p>&nbsp;</p>
		<h3>Upload a TEI Corpus file to verify Names and Dates:</h3>
		<div class="form_row">
			<form enctype="multipart/form-data" action="../../api/teiNamesAndDates.php" method="POST">
				<!-- MAX_FILE_SIZE must precede the file input field -->
				<input type="hidden" name="MAX_FILE_SIZE" value="{$maxfilesize}" />
				<!-- Name of input element determines name in $_FILES array -->
				<table class="form_row" border="0" cellspacing="0" cellpadding="4px">
					<tr height="40px">
						<td class="corpus corpusname 2" width="200px">Select TEI file to upload:</td>
						<td colspan="2" class="corpus corpusdesc 2" >
							<input name="teifile" type="file" size="44"  />
							&nbsp;<input type="submit" value="Upload File" />
						</td>
					</tr>
				</table>
			</form>
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
