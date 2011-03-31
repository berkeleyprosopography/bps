{include file="header.tpl"}

	{if !isset($corpus) }
		<h1>Error: No corpus specified!</h1>
	{else}
		<h1>Administer Corpus: {$corpus.name}</h1>
		{if isset($errmsg) }
			<h2>{$errmsg}</h2>
		{else}
			<p>Description:&nbsp:
				<textarea id="D_{$corpus.id}" cols="40" rows="2"
						onkeyup="enableElement('U_{$corpus.id}',true);setStatusP('')">
					{$corpus.description}
				</textarea>
				<input disabled id="U_{$corpus.id}" type="button"
						onclick="updateCorpus('{$corpus.id}','{$corpus.name}')" value=" Update " />
			</p>
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
	<p>&nbsp;</p>
	{if isset($opmsg) }
		<p id="statusP">{$opmsg}</p>
	{else}
		<p id="statusP"> &nbsp; </p>
	{/if}
{/if}
{include file="footer.tpl"}
